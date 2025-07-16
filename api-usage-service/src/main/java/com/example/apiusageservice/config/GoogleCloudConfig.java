package com.example.apiusageservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCloudConfig {

    @Value("${google.project-id}")
    private String projectId;

    @Value("${google.credentials-path}")
    private String credentialsPath;

    @Bean
    public String getProjectId() {
        return projectId;
    }

    @Bean
    public String getCredentialsPath() {
        return credentialsPath;
    }
}
