package com.ttn.punchingSystem.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

}
