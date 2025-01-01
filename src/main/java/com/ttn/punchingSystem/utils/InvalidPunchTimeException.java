package com.ttn.punchingSystem.utils;

public class InvalidPunchTimeException extends Exception {
    public InvalidPunchTimeException(String message) {
        super(message);
    }

    public InvalidPunchTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

