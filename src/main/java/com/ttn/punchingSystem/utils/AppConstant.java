package com.ttn.punchingSystem.utils;

import java.util.regex.Pattern;

public class AppConstant {
    public static final String DATE_FORMAT = "dd MMM yyyy h:mm a";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^\\d{2}[A-Za-z]{3}\\d{4}_punchdetails\\.csv$"
    );
}
