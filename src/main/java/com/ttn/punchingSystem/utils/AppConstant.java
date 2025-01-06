package com.ttn.punchingSystem.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AppConstant {
    public static final String DATE_FORMAT = "dd MMM yyyy h:mm a";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^\\d{2}[A-Za-z]{3}\\d{4}_punchdetails\\.csv$"
    );
    public static final Map<String, String> EMAIL_KEYS = new HashMap<>();
    static {
        EMAIL_KEYS.put("SMTP_HOST", "Missing or invalid SMTP_HOST in secrets.");
        EMAIL_KEYS.put("SMTP_PORT", "Missing or invalid SMTP_PORT in secrets.");
        EMAIL_KEYS.put("SMTP_AUTH", "Missing or invalid SMTP_AUTH in secrets.");
        EMAIL_KEYS.put("SMTP_STARTTLS", "Missing or invalid SMTP_STARTTLS in secrets.");
        EMAIL_KEYS.put("SENDER_EMAIL", "Missing or invalid SENDER_EMAIL in secrets.");
        EMAIL_KEYS.put("SENDER_PASSWORD", "Missing or invalid SENDER_PASSWORD in secrets.");
    }
    public static final String AWS_REGION = "ap-south-1";
}
