package com.ttn.punchingSystem.utils;

import java.util.List;

public class CsvValidationException extends RuntimeException {
    private List<String> errorMessages;

    public CsvValidationException(List<String> errorMessages) {
        super("Validation errors occurred while processing the CSV file.");
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}

