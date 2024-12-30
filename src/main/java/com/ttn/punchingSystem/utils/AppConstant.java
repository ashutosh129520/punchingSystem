package com.ttn.punchingSystem.utils;

import java.util.regex.Pattern;

public class AppConstant {
    public static final String DATE_FORMAT = "dd MMM yyyy h:mm a";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
}
