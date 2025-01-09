package com.ttn.punchingSystem.config;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Service
public class S3CsvReaderService {

    private final S3Client s3Client;

    public S3CsvReaderService() {
        this.s3Client = S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
    }

    public S3CsvReaderService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public S3Client getS3Client(){
        if (this.s3Client == null) {
            throw new IllegalStateException("S3Client is not initialized");
        }
        return this.s3Client;
    }

}
