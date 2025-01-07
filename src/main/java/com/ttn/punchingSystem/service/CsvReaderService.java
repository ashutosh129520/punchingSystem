package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.repository.ProjectRepository;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.repository.WorkScheduleRepository;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.CsvValidationException;
import com.ttn.punchingSystem.utils.DateUtil;
import com.ttn.punchingSystem.utils.InvalidPunchTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class CsvReaderService {

    @Autowired
    private PunchLogRepository punchLogRepository;
    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFile(String filePath) throws ParseException, InvalidPunchTimeException {
        List<PunchingDetailsDTO> punchDataList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        String fileName = validateFileName(filePath);
        punchDataList = readCsvFileIntoDTO(punchDataList, filePath, errorList, fileName);
        if (!errorList.isEmpty()) {
            throw new CsvValidationException(errorList);
        }
        Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
        saveProcessedPunchLogs(userPunchTimes);
        return ResponseEntity.status(HttpStatus.OK).body(punchDataList);
    }

    public List<PunchingDetailsDTO> readCsvFileIntoDTO(List<PunchingDetailsDTO> punchDataList, String filePath, List<String> errorList, String fileName){
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
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

    public String validateFileName(String filePath) {
        String fileName = extractFileName(filePath);
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid file name format. Expected format: 01Jan2024_punchdetails.csv");
        }
        return fileName;
    }

    private String extractFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
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
        List<WorkScheduleDetails> workSchedules = workScheduleRepository.findAllByUserEmailIn(listOfEmails);
        for (WorkScheduleDetails workSchedule : workSchedules) {
            if(Objects.nonNull(workSchedule.getProject())) {
                reportingEmail = workSchedule.getProject().getReportingManagerEmail();
            }
            String userEmail = workSchedule.getUserEmail();
            List<PunchingDetails> defaultersPunchingDetails = listOfDefaulters.getOrDefault(userEmail, new ArrayList<>());
            managerToDefaultersMap.computeIfAbsent(reportingEmail, k -> new ArrayList<>()).addAll(defaultersPunchingDetails);
        }
        return managerToDefaultersMap;
    }
}
