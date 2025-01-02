package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CsvReaderService {

    @Autowired
    private PunchLogRepository punchLogRepository;
    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);

    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFile(String filePath) throws ParseException, InvalidPunchTimeException {
        List<PunchingDetailsDTO> punchDataList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        String fileName = validateFileName(filePath);
        punchDataList = readCsvFileIntoDTO(punchDataList, filePath, errorList, fileName);
        if (!errorList.isEmpty()) {
            throw new CsvValidationException(errorList);
        }
        Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
        Map<String, WorkScheduleDetails> workScheduleMap = fetchWorkScheduleUsersBasedOnEmailId(userPunchTimes);
        saveProcessedPunchLogs(userPunchTimes);
        return ResponseEntity.status(HttpStatus.OK).body(punchDataList);
    }

    private List<PunchingDetailsDTO> readCsvFileIntoDTO(List<PunchingDetailsDTO> punchDataList, String filePath, List<String> errorList, String fileName){
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

    private Map<String, WorkScheduleDetails> fetchWorkScheduleUsersBasedOnEmailId(Map<String, List<Date>> userPunchTimes) {
        Set<String> userEmails = userPunchTimes.keySet();
        Set<WorkScheduleDetails> workSchedules = workScheduleRepository.findAllByUserEmailIn(userEmails);
        Map<String, WorkScheduleDetails> workScheduleMap = workSchedules.stream().collect(Collectors.toMap(WorkScheduleDetails::getUserEmail, ws -> ws));
        return workScheduleMap;
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

    private String validateFileName(String filePath) {
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

    private Map<String, List<Date>> groupPunchTimesByUser(List<PunchingDetailsDTO> punchDataList) throws ParseException {
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

    private void saveProcessedPunchLogs(Map<String, List<Date>> userPunchTimes) throws ParseException, InvalidPunchTimeException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //Map<String, PunchingDetails> processedLogs = new HashMap<>();
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

    private void saveLogsToRepository(Collection<PunchingDetails> logs) {
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
}
