package com.ttn.punchingSystem.config;

import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class S3CsvReaderService {

    private final S3Client s3Client;

    public S3CsvReaderService(@Value("${aws.region}") String region) {
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

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

    public ResponseInputStream<?> getS3Object(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(AppConstant.AWS_BUCKET)
                .key(fileName)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

}
