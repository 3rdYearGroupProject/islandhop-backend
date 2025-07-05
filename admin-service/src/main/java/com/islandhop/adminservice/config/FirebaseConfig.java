package com.islandhop.adminservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase configuration for admin service.
 * Initializes Firebase app for status monitoring.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-key}")
    private String serviceAccountKeyPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase app is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Load service account key
                InputStream serviceAccount;
                
                if (serviceAccountKeyPath.startsWith("classpath:")) {
                    String resourcePath = serviceAccountKeyPath.substring("classpath:".length());
                    serviceAccount = new ClassPathResource(resourcePath).getInputStream();
                } else {
                    throw new IllegalArgumentException("Service account key path must start with 'classpath:'");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully for project: {}", projectId);
            } else {
                logger.info("Firebase app already initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        } catch (Exception e) {
            logger.error("Unexpected error during Firebase initialization: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        }
    }
}
