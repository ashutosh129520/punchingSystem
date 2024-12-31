package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CsvReaderService {

    @Autowired
    private PunchLogRepository punchLogRepository;

    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFile(String filePath) throws ParseException {
        List<PunchingDetailsDTO> punchDataList = new ArrayList<>();
        validateFileName(filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                PunchingDetailsDTO punchingDetailsDTO = new PunchingDetailsDTO(data[0], data[1]);
                if (isValidPunchData(punchingDetailsDTO)) {
                    punchDataList.add(punchingDetailsDTO);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the CSV file: " + e.getMessage());
        }
            Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
            saveProcessedPunchLogs(userPunchTimes);

        return ResponseEntity.status(HttpStatus.OK).body(punchDataList);
    }

    private boolean isValidPunchData(PunchingDetailsDTO punchData) {
        return isValidEmail(punchData.getUserEmail()) && isValidDate(punchData.getPunchTime());
    }

    private boolean isValidEmail(String email) {
        return AppConstant.EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidDate(String dateStr) {
        boolean isValidDate = DateUtil.isValidDateFormat(dateStr);
        return isValidDate;
    }

    private void validateFileName(String fileName) {
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException(
                    "Invalid file name format. Expected format: 19Oct2024_punchdetails.csv"
            );
        }
    }

    private Map<String, List<Date>> groupPunchTimesByUser(List<PunchingDetailsDTO> punchDataList) throws ParseException {
        Map<String, List<Date>> userPunchTimes = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);

        for (PunchingDetailsDTO punchData : punchDataList) {
            String userEmail = punchData.getUserEmail();
            Date punchTime = sdf.parse(punchData.getPunchTime());

            userPunchTimes
                    .computeIfAbsent(userEmail, k -> new ArrayList<>())
                    .add(punchTime);
        }

        userPunchTimes.values().forEach(Collections::sort);
        return userPunchTimes;
    }

    private void saveProcessedPunchLogs(Map<String, List<Date>> userPunchTimes) {
        for (Map.Entry<String, List<Date>> entry : userPunchTimes.entrySet()) {
            String userEmail = entry.getKey();
            List<Date> times = entry.getValue();

            if (times.isEmpty()) continue;

            Date punchIn = times.get(0);
            Date punchOut = times.get(times.size() - 1);

            PunchingDetails punchingDetails = new PunchingDetails();
            punchingDetails.setUserEmail(userEmail);
            punchingDetails.setPunchDate(punchIn);
            punchingDetails.setPunchInTime(punchIn);
            punchingDetails.setPunchOutTime(punchOut);

            // Check for duplicate data
            if (punchLogRepository.findByUserEmailAndPunchDate(userEmail, punchIn).isEmpty()) {
                punchLogRepository.save(punchingDetails);
            }
        }
    }
}
