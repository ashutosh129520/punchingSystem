package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.model.WorkScheduleResult;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.repository.WorkScheduleRepository;
import com.ttn.punchingSystem.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvReaderService {

    @Autowired
    private PunchLogRepository punchLogRepository;
    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private S3CsvReaderService s3CsvReaderService;
    @Autowired
    private CacheService cacheService;
    @Value("${spring.aws.bucketName}")
    private String bucketName;
    @Autowired
    private CacheMetrics cacheMetrics;

    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFileFromS3() {
        String fileName = generateLastDayFileNameToReadFromS3();
        List<PunchingDetailsDTO> punchDataList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = S3ProcessingService.processS3Object(fileName, s3CsvReaderService);
            fileName = validateFileName(fileName);
            punchDataList = readCsvFileIntoDTO(punchDataList, bufferedReader, errorList, fileName);
            if (!errorList.isEmpty()) {
                throw new CsvValidationException(errorList);
            }
            Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
            saveProcessedPunchLogs(userPunchTimes);
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV from S3: " + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(punchDataList);
    }

    public String generateLastDayFileNameToReadFromS3(){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT_FOR_FILE); // Adjust to match your file naming format
        String yesterdayFormatted = yesterday.format(formatter);
        String fileKey = yesterdayFormatted + AppConstant.POSTFIX_FILE_NAME;
        return fileKey;
    }

    public List<PunchingDetailsDTO> readCsvFileIntoDTO(List<PunchingDetailsDTO> punchDataList, BufferedReader br, List<String> errorList, String fileName){
        try{
            String line;
            line = br.readLine();
            validateHeaderNames(errorList, line);
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 2) {
                    errorList.add("Invalid row format: " + line);
                    continue;
                }
                PunchingDetailsDTO punchingDetailsDTO = new PunchingDetailsDTO(
                        (data[0] == null || data[0].isEmpty()) ? null : data[0],
                        (data[1] == null || data[1].isEmpty()) ? null : data[1]
                );
                if (!isValidPunchData(punchingDetailsDTO, fileName, errorList)) {
                    continue;
                }
                punchDataList.add(punchingDetailsDTO);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error reading the CSV file: " + e.getMessage());
        }
        return punchDataList;
    }

    public void validateHeaderNames(List<String> errorList, String line){
        String[] header = line.split(",");
        if (header.length < 2 || !header[0].equalsIgnoreCase("userEmail") || !header[1].equalsIgnoreCase("punchTime")) {
            errorList.add("Invalid header format. Expected header: userEmail,punchTime");
        }
    }

    private boolean isValidPunchData(PunchingDetailsDTO punchingDetailsDTO, String fileName, List<String> errorList) throws ParseException {
        boolean isValid = true;
        fileName = DateUtil.parseFileNameDate(fileName);
        String punchInDate = DateUtil.parsePunchDate(punchingDetailsDTO.getPunchTime());
        if (!isValidEmail(punchingDetailsDTO.getUserEmail())) {
            errorList.add("Invalid email: " + punchingDetailsDTO.getUserEmail());
            isValid = false;
        }
        if(!fileName.equals(punchInDate)){
            errorList.add("PunchedDate does not correspond to fileDate" + punchingDetailsDTO.getPunchTime());
        }
        if (!DateUtil.isValidDateFormat(punchingDetailsDTO.getPunchTime())) {
            errorList.add("Invalid date format: " + punchingDetailsDTO.getPunchTime());
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidEmail(String email) {
        return AppConstant.EMAIL_PATTERN.matcher(email).matches();
    }

    public String validateFileName(String fileName) {
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid file name format. Expected format: 01Jan2024_punchdetails.csv");
        }
        return fileName;
    }

    public Map<String, List<Date>> groupPunchTimesByUser(List<PunchingDetailsDTO> punchDataList) throws ParseException {
        Map<String, List<Date>> userPunchTimes = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);
        for (PunchingDetailsDTO punchData : punchDataList) {
            String userEmail = punchData.getUserEmail();
            Date punchTime = sdf.parse(punchData.getPunchTime());
            if(!userPunchTimes.containsKey(userEmail)){
                userPunchTimes.put(userEmail, new ArrayList<>());
            }
            userPunchTimes.get(userEmail).add(punchTime);
        }
        userPunchTimes.values().forEach(Collections::sort);
        return userPunchTimes;
    }

    public void saveProcessedPunchLogs(Map<String, List<Date>> userPunchTimes) throws InvalidPunchTimeException {
        List<PunchingDetails> processedLogs = new ArrayList<>();
            for (Map.Entry<String, List<Date>> entry : userPunchTimes.entrySet()) {
                String userEmail = entry.getKey();
                List<Date> times = entry.getValue();
                if (times.isEmpty()) continue;
                Date punchIn = times.get(0);
                Date punchOut = times.size() > 1 ? entry.getValue().get(times.size() - 1) : null;
                if(validateTimes(punchIn, punchOut, userEmail)) {
                    PunchingDetails punchingDetails = new PunchingDetails();
                    punchingDetails.setUserEmail(userEmail);
                    punchingDetails.setPunchDate(punchIn);
                    punchingDetails.setPunchInTime(punchIn);
                    punchingDetails.setPunchOutTime(punchOut);
                    processedLogs.add(punchingDetails);
                }
            }
        saveLogsToRepository(processedLogs);
    }

    private static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(Objects.nonNull(date1) && Objects.nonNull(date2)) {
            return dayFormat.format(date1).equals(dayFormat.format(date2));
        }
        return true;
    }

    private boolean validateTimes(Date punchIn, Date punchOut, String userEmail) throws InvalidPunchTimeException {
        if (!isSameDay(punchIn, punchOut)) {
            throw new InvalidPunchTimeException("Invalid punch time in csv file for user: " + userEmail);
        }
        if(Objects.isNull(punchOut) && Objects.nonNull(punchIn)){
            return true;
        }
        // Check if punchOut time is valid for the same day
        if (punchOut.before(punchIn)) {
            System.out.println("Invalid: punchOut cannot be before punchIn for the same day.");
            return false;
        }
        return true;
    }

    public void saveLogsToRepository(Collection<PunchingDetails> logs) {
        for (PunchingDetails log : logs) {
            List<PunchingDetails> existingLogs = punchLogRepository.findByUserEmailAndPunchDate(
                    log.getUserEmail(),
                    log.getPunchDate()
            );
            if (existingLogs.isEmpty()) {
                punchLogRepository.save(log);
            }else {
                PunchingDetails existingLog = existingLogs.get(0);
                boolean isUpdated = false;
                if (!log.getPunchInTime().equals(existingLog.getPunchInTime())) {
                    existingLog.setPunchInTime(log.getPunchInTime());
                    isUpdated = true;
                }
                if(Objects.isNull(log.getPunchOutTime())) {
                        existingLog.setPunchOutTime(null);
                        isUpdated = true;
                }else if(!log.getPunchOutTime().equals(existingLog.getPunchOutTime())){
                        existingLog.setPunchOutTime(log.getPunchOutTime());
                        isUpdated = true;
                }
                if (isUpdated) {
                    punchLogRepository.save(existingLog);
                }
            }
        }
    }

    public Map<String, List<PunchingDetails>> processListOfDefaulters(){
        Map<String, List<PunchingDetails>> listOfDefaulters = new HashMap<>();
        Map<String, List<PunchingDetails>> projectIdsAndDefaultersEmail = new HashMap<>();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date previousDay = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<PunchingDetails> previousDayPunchingDetails = punchLogRepository.findByPunchDate(previousDay);
        for(PunchingDetails punchingDetails : previousDayPunchingDetails){
            Date punchInTime = punchingDetails.getPunchInTime();
            Date punchOutTime = punchingDetails.getPunchOutTime();
            if (punchInTime != null) {
                if (punchOutTime != null) {
                    long durationInMillis = punchOutTime.getTime() - punchInTime.getTime();
                    long durationInHours = durationInMillis / (1000 * 60 * 60);
                    if (durationInHours < 6) {
                        punchingDetails.setDurationInHours(durationInHours);
                        listOfDefaulters.computeIfAbsent(punchingDetails.getUserEmail(), k -> new ArrayList<>()).add(punchingDetails);
                    }
                } else {
                    punchingDetails.setDurationInHours(0);
                    listOfDefaulters.computeIfAbsent(punchingDetails.getUserEmail(), k -> new ArrayList<>()).add(punchingDetails);
                }
            }
        }
        if(!listOfDefaulters.isEmpty()){
            projectIdsAndDefaultersEmail = projectIdAndDefaultersEmailMap(listOfDefaulters);
        }
        return projectIdsAndDefaultersEmail;
    }

    public Map<String, List<PunchingDetails>> projectIdAndDefaultersEmailMap(Map<String, List<PunchingDetails>> listOfDefaulters){
        Map<String, List<PunchingDetails>> managerToDefaultersMap = new HashMap<>();
        List<String> listOfEmails = new ArrayList<>(listOfDefaulters.keySet());
        String reportingEmail = "";
        WorkScheduleResult workSchedules = cacheService.getCachedWorkSchedulesDefaulterList(listOfEmails);
        if (workSchedules.isFromCache()) {
            cacheMetrics.incrementCacheHit(AppConstant.DEFAULTERS_CACHE_METRIC);
        } else {
            cacheMetrics.incrementCacheMiss(AppConstant.DEFAULTERS_CACHE_METRIC);
        }
        for (WorkScheduleDetails workSchedule : workSchedules.getWorkSchedules()) {
            if(Objects.nonNull(workSchedule.getProject())) {
                reportingEmail = workSchedule.getProject().getReportingManagerEmail();
            }
            String userEmail = workSchedule.getUserEmail();
            List<PunchingDetails> defaultersPunchingDetails = listOfDefaulters.getOrDefault(userEmail, new ArrayList<>());
            if(!managerToDefaultersMap.containsKey(reportingEmail)){
                managerToDefaultersMap.put(reportingEmail, new ArrayList<>());
            }
            managerToDefaultersMap.get(reportingEmail).addAll(defaultersPunchingDetails);
        }
        return managerToDefaultersMap;
    }
}
