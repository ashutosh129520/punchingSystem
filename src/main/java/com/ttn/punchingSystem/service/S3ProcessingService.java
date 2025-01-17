package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class S3ProcessingService {

    public static BufferedReader processS3Object(String fileName, S3CsvReaderService s3CsvReaderService) {
        try {
            boolean fileExists = checkIfFileExistsInS3(fileName, s3CsvReaderService);
            if (!fileExists) {
                throw new RuntimeException("File not found in S3: " + fileName);
            }
            ResponseInputStream<?> s3ObjectStream = s3CsvReaderService.getS3Object(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(s3ObjectStream, StandardCharsets.UTF_8);
            return new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            throw new RuntimeException("Error getting BufferedReader from S3: " + e.getMessage(), e);
        }
    }

    public static boolean checkIfFileExistsInS3(String fileName, S3CsvReaderService s3CsvReaderService) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(AppConstant.AWS_BUCKET).key(fileName).build();
            HeadObjectResponse headObjectResponse = s3CsvReaderService.getS3Client().headObject(headObjectRequest);
            return headObjectResponse != null;
        } catch (S3Exception e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error checking file existence in S3: " + e.getMessage(), e);
        }
    }
}
