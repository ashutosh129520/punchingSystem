package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchDetailsWrapper;
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

    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFile(String filePath) throws ParseException {
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
                /*if (isValidPunchData(punchingDetailsDTO)) {
                    punchDataList.add(punchingDetailsDTO);
                }*/
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

    /*private boolean isValidDate(String dateStr) {
        boolean isValidDate = DateUtil.isValidDateFormat(dateStr);
        return isValidDate;
    }*/

    private void validateFileName(String filePath) {
        String fileName = extractFileName(filePath);
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid file name format. Expected format: 19Oct2024_punchdetails.csv");
        }
    }

    private String extractFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString(); // Extracts and returns only the file name
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

    private void saveProcessedPunchLogs(Map<String, List<Date>> userPunchTimes) {
        for (Map.Entry<String, List<Date>> entry : userPunchTimes.entrySet()) {
            List<Date> times = entry.getValue();
            if (times.isEmpty()) continue;
            PunchDetailsWrapper wrapper = PunchingDetailsMapper.INSTANCE.mapToWrapper(entry);
            PunchingDetails punchingDetails = new PunchingDetails();
            PunchingDetailsMapper.INSTANCE.updatePunchingDetails(punchingDetails, wrapper);
            // Check for duplicate data
            if (punchLogRepository.findByUserEmailAndPunchDate(wrapper.getUserEmail(), wrapper.getPunchIn()).isEmpty()) {
                punchLogRepository.save(punchingDetails);
            }
        }
    }
}
