package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchData;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CsvReaderService {

    public List<PunchData> readCsvFile() {
        List<PunchData> punchDataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/sample_punch_data.csv")))
        ) {
            // Skip header and parse the rest
            punchDataList = reader.lines()
                    .skip(1)
                    .map(line -> {
                        String[] fields = line.split(",");
                        PunchData punchData = new PunchData();
                        punchData.setUserEmail(fields[0].trim());
                        punchData.setPunchTime(fields[1].trim());
                        return punchData;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return punchDataList;
    }

    public List<PunchData> readAndValidateCsvFile() {
        List<PunchData> validPunchDataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/sample_punch_data.csv")))
        ) {
            validPunchDataList = reader.lines()
                    .skip(1) // Skip header row
                    .map(this::parseLine)
                    .filter(this::isValidPunchData)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return validPunchDataList;
    }

    private PunchData parseLine(String line) {
        String[] fields = line.split(",");
        PunchData punchData = new PunchData();
        punchData.setUserEmail(fields[0].trim());
        punchData.setPunchTime(fields[1].trim());
        return punchData;
    }

    private boolean isValidPunchData(PunchData punchData) {
        return isValidEmail(punchData.getUserEmail()) && isValidDate(punchData.getPunchTime());
    }

    private boolean isValidEmail(String email) {
        return AppConstant.EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);
            sdf.setLenient(false); // Ensure strict parsing
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
