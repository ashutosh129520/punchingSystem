package com.ttn.punchingSystem.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecretsManagerConfig {

    private final AWSSecretsManager secretsManager;

    @Value("${mail.aws.accessKey}")
    private String accessKey;

    @Value("${mail.aws.secretKey}")
    private String secretKey;

    public SecretsManagerConfig(@Value("${mail.aws.accessKey}") String accessKey,
                                @Value("${mail.aws.secretKey}") String secretKey) {
        this.secretsManager = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(AppConstant.AWS_REGION)
                .build();
    }

    public JsonObject getSecrets(String secretName) {
        JsonObject jsonObjectSecret = null;
            GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);
            GetSecretValueResult result = secretsManager.getSecretValue(request);
            jsonObjectSecret = JsonParser.parseString(result.getSecretString()).getAsJsonObject();
        return jsonObjectSecret;
    }
}
