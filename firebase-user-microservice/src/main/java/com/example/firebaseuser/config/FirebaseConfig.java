package com.example.firebaseuser.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Initializes Firebase Admin SDK using service account key from application.properties.
 */
@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service.account.key}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Firebase Admin SDK...");
            logger.debug("Service account key path: {}", serviceAccountKeyPath);

            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(serviceAccountKeyPath);
            if (serviceAccount == null) {
                logger.error("Service account file not found at: {}", serviceAccountKeyPath);
                throw new RuntimeException("Service account file not found at: " + serviceAccountKeyPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK initialized successfully.");
            } else {
                logger.warn("Firebase Admin SDK was already initialized.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase Admin SDK", e);
            throw new RuntimeException("Firebase initialization error", e);
        }
    }
}