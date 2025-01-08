package com.ttn.punchingSystem.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AppConstant {
    public static final String DATE_FORMAT = "dd MMM yyyy h:mm a";
    public static final String DATE_FORMAT_FOR_FILE = "ddMMMyyyy";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^\\d{2}[A-Za-z]{3}\\d{4}_punchdetails\\.csv$"
    );
    public static final Map<String, String> EMAIL_KEYS = new HashMap<>();
    static {
        EMAIL_KEYS.put("SENDER_EMAIL", "Missing or invalid SENDER_EMAIL in secrets.");
        EMAIL_KEYS.put("SENDER_PASSWORD", "Missing or invalid SENDER_PASSWORD in secrets.");
    }
    public static final String AWS_REGION = "ap-south-1";
    public static final String DEFAULTERS_REPORT = "Defaulters Report";
    public static final String DEFAULTERS_REPORT_NAME = "defaulters-report";
    public static final String REPORTING_MANAGER_MAIL = "reportingManagerEmail";
    public static final String DEFAULTERS = "defaulters";
    public static final String AWS_BUCKET = "punchingsystembucket";
    public static final String POSTFIX_FILE_NAME = "_punchdetails.csv";
}
