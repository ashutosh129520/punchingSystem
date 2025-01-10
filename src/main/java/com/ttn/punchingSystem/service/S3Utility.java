package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.config.S3CsvReaderService;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class S3Utility {

    public static BufferedReader processS3Object(String fileName, S3CsvReaderService s3CsvReaderService) {
        try {
            ResponseInputStream<?> s3ObjectStream = s3CsvReaderService.getS3Object(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(s3ObjectStream, StandardCharsets.UTF_8);
            return new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            throw new RuntimeException("Error getting BufferedReader from S3: " + e.getMessage(), e);
        }
    }
}
