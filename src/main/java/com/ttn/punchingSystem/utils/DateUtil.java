package com.ttn.punchingSystem.utils;

import com.ttn.punchingSystem.model.PunchingDetailsDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    public static boolean isValidDateFormat(String date){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);
            sdf.setLenient(false); // Ensure strict parsing
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String parseFileNameDate(String fileName) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMMyyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        fileName = fileName.substring(0,9);
        Date parseDate = inputFormat.parse(fileName);
        fileName = outputFormat.format(parseDate);
        return fileName;
    }

    public static String parsePunchDate(String punchDate) throws ParseException {
        punchDate = punchDate.substring(0,11);
        return punchDate;
    }

    public static String getFormattedTodaysDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return today.format(formatter);
    }

}
