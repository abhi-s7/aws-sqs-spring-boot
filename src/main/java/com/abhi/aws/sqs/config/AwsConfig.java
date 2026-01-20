package com.abhi.aws.sqs.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AwsSqsProperties.class)
public class AwsConfig {
    
    private final AwsSqsProperties awsSqsProperties;
    
    @Autowired
    public AwsConfig(AwsSqsProperties awsSqsProperties) {
        this.awsSqsProperties = awsSqsProperties;
    }

    @Bean
    public SqsClient sqsClient() {
        String accessKey = awsSqsProperties.getAccessKey();
        String secretKey = awsSqsProperties.getSecretKey();
        
        // Check if explicit credentials are provided (local development with .env)
        if (accessKey != null && !accessKey.trim().isEmpty() 
            && secretKey != null && !secretKey.trim().isEmpty()) {
            // Local: Use explicit credentials from .env file
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            return SqsClient.builder()
                    .region(Region.of(awsSqsProperties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .build();
        } else {
            // EC2: Use default credential chain (IAM role from instance metadata)
            return SqsClient.builder()
                    .region(Region.of(awsSqsProperties.getRegion()))
                    .build();
        }
    }
}
