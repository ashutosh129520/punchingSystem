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
        validateFileName(filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 2) {
                    errorList.add("Invalid row format: " + line);
                    continue;
                }
                PunchingDetailsDTO punchingDetailsDTO = new PunchingDetailsDTO(data[0], data[1]);
                if (!isValidPunchData(punchingDetailsDTO, errorList)) {
                    continue;
                }
                punchDataList.add(punchingDetailsDTO);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the CSV file: " + e.getMessage());
        }
        if (!errorList.isEmpty()) {
            throw new CsvValidationException(errorList);
        }
        Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
        Map<String, WorkScheduleDetails> workScheduleMap = fetchWorkScheduleUsersBasedOnEmailId(userPunchTimes);
        saveProcessedPunchLogs(userPunchTimes);
        return ResponseEntity.status(HttpStatus.OK).body(punchDataList);
    }

    private Map<String, WorkScheduleDetails> fetchWorkScheduleUsersBasedOnEmailId(Map<String, List<Date>> userPunchTimes) {
        Set<String> userEmails = userPunchTimes.keySet();
        Set<WorkScheduleDetails> workSchedules = workScheduleRepository.findAllByUserEmailIn(userEmails);
        Map<String, WorkScheduleDetails> workScheduleMap = workSchedules.stream().collect(Collectors.toMap(WorkScheduleDetails::getUserEmail, ws -> ws));
        return workScheduleMap;
    }

    private boolean isValidPunchData(PunchingDetailsDTO punchingDetailsDTO, List<String> errorList) {
        boolean isValid = true;
        if (!isValidEmail(punchingDetailsDTO.getUserEmail())) {
            errorList.add("Invalid email: " + punchingDetailsDTO.getUserEmail());
            isValid = false;
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

    private void validateFileName(String filePath) {
        String fileName = extractFileName(filePath);
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid file name format. Expected format: 19Oct2024_punchdetails.csv");
        }
    }

    private String extractFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }

    private Date parsePunchTime(String punchTimeStr, SimpleDateFormat sdf) throws InvalidPunchTimeException {
        try {
            return sdf.parse(punchTimeStr);
        } catch (ParseException e) {
            throw new InvalidPunchTimeException("Invalid punch time format: " + punchTimeStr, e);
        }
    }

    private Map<String, List<Date>> groupPunchTimesByUser(List<PunchingDetailsDTO> punchDataList) {
        return punchDataList.stream()
                .map(punchData -> {
                    try {
                        return new AbstractMap.SimpleEntry<>(punchData.getUserEmail(), parsePunchTime(punchData.getPunchTime(), sdf));
                    } catch (InvalidPunchTimeException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ))
                .entrySet().stream()
                .peek(entry -> Collections.sort(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void saveProcessedPunchLogs(Map<String, List<Date>> userPunchTimes) throws ParseException, InvalidPunchTimeException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, PunchingDetails> processedLogs = new HashMap<>();
            for (Map.Entry<String, List<Date>> entry : userPunchTimes.entrySet()) {
                String userEmail = entry.getKey();
                List<Date> times = entry.getValue();
                if (times.isEmpty()) continue;
                Date punchIn = times.get(0);
                Date punchOut = times.size() > 1 ? entry.getValue().get(times.size() - 1) : null;
                String punchInDate = sdf.format(punchIn);
                //String punchOutDate = punchOut != null ? sdf.format(punchOut) : punchInDate;
                if(validateTimes(punchIn, punchOut, userEmail)) {
                    PunchingDetails punchingDetails = new PunchingDetails();
                    punchingDetails.setUserEmail(userEmail);
                    punchingDetails.setPunchDate(punchIn);
                    punchingDetails.setPunchInTime(punchIn);
                    punchingDetails.setPunchOutTime(punchOut);
                    if (!processedLogs.containsKey(userEmail + punchInDate)) {
                        processedLogs.put(userEmail + punchInDate, punchingDetails);
                    }
                }
            }
        saveLogsToRepository(processedLogs.values());
    }

    private static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dayFormat.format(date1).equals(dayFormat.format(date2));
    }

    private boolean validateTimes(Date punchIn, Date punchOut, String userEmail) throws InvalidPunchTimeException {
        if (!isSameDay(punchIn, punchOut)) {
            throw new InvalidPunchTimeException("Invalid punch time in csv file for user: " + userEmail);
        }
        // Check if punchOut time is valid for the same day
        if (punchOut.before(punchIn)) {
            System.out.println("Invalid: punchOut cannot be before punchIn for the same day.");
            return false;
        }
        return false;
    }

    private void saveLogsToRepository(Collection<PunchingDetails> logs) {
        for (PunchingDetails log : logs) {
            if (punchLogRepository.findByUserEmailAndPunchDate(log.getUserEmail(), log.getPunchDate()).isEmpty()) {
                punchLogRepository.save(log);
            }
        }
    }
}
