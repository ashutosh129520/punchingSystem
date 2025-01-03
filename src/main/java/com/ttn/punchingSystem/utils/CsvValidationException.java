package com.ttn.punchingSystem.utils;

import java.util.List;

public class CsvValidationException extends RuntimeException {

    public CsvValidationException(List<String> errorMessages) {
        super("Validation errors occurred while processing the CSV file: "+errorMessages);
    }
}

