package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchData;
import com.ttn.punchingSystem.model.PunchLog;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CsvReaderService {

    @Autowired
    private PunchLogRepository punchLogRepository;

    public List<PunchData> readCsvFile(String filePath) throws ParseException {
        List<PunchData> punchDataList = new ArrayList<>();
        validateFileName(filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                PunchData punchData = new PunchData(data[0], data[1]);
                if (isValidPunchData(punchData)) {
                    punchDataList.add(punchData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the CSV file: " + e.getMessage());
        }
            Map<String, List<Date>> userPunchTimes = groupPunchTimesByUser(punchDataList);
            saveProcessedPunchLogs(userPunchTimes);

        return punchDataList;
    }

    /*private PunchData parseLine(String line) {
        String[] fields = line.split(",");
        PunchData punchData = new PunchData();
        punchData.setUserEmail(fields[0].trim());
        punchData.setPunchTime(fields[1].trim());
        return punchData;
    }*/

    private boolean isValidPunchData(PunchData punchData) {
        return isValidEmail(punchData.getUserEmail()) && isValidDate(punchData.getPunchTime());
    }

    private boolean isValidEmail(String email) {
        return AppConstant.EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidDate(String dateStr) {
        boolean isValidDate = DateUtil.isValidDateFormat(dateStr);
        return isValidDate;
    }

    /*public List<PunchData> readAndValidateCsvFile(String fileName) {
        validateFileName(fileName);

        List<PunchData> validPunchDataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/" + fileName)))
        ) {
            validPunchDataList = reader.lines()
                    .skip(1)
                    .map(this::parseLine)
                    .filter(this::isValidPunchData)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return validPunchDataList;
    }*/
    private void validateFileName(String fileName) {
        if (!AppConstant.FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException(
                    "Invalid file name format. Expected format: 19Oct2024_punchdetails.csv"
            );
        }
    }

    private Map<String, List<Date>> groupPunchTimesByUser(List<PunchData> punchDataList) throws ParseException {
        Map<String, List<Date>> userPunchTimes = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);

        for (PunchData punchData : punchDataList) {
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

            PunchLog punchLog = new PunchLog();
            punchLog.setUserEmail(userEmail);
            punchLog.setPunchDate(punchIn);
            punchLog.setPunchInTime(punchIn);
            punchLog.setPunchOutTime(punchOut);

            // Check for duplicate data
            if (punchLogRepository.findByUserEmailAndPunchDate(userEmail, punchIn).isEmpty()) {
                punchLogRepository.save(punchLog);
            }
        }
    }
}
