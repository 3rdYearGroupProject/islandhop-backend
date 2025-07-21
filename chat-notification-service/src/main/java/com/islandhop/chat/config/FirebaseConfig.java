package com.islandhop.chat.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase configuration for authentication and services.
 * Initializes Firebase Admin SDK for token verification.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Value("${firebase.project.id:}")
    private String projectId;

    /**
     * Initialize Firebase App on application startup.
     */
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase app is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("Initializing Firebase Admin SDK...");

                // Try to load from service account file first
                FirebaseOptions options = null;
                
                try {
                    // Try to load from classpath
                    ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                    if (resource.exists()) {
                        try (InputStream serviceAccount = resource.getInputStream()) {
                            options = FirebaseOptions.builder()
                                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                    .build();
                            logger.info("Firebase initialized with service account file: {}", firebaseConfigPath);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Could not load Firebase service account from file: {}", e.getMessage());
                }

                // If file loading failed, try to use environment variables or default credentials
                if (options == null) {
                    try {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.getApplicationDefault())
                                .setProjectId(projectId)
                                .build();
                        logger.info("Firebase initialized with application default credentials");
                    } catch (Exception e) {
                        logger.warn("Could not initialize Firebase with default credentials: {}", e.getMessage());
                        
                        // Last resort: initialize without credentials for development
                        options = FirebaseOptions.builder()
                                .setProjectId(projectId.isEmpty() ? "islandhop-dev" : projectId)
                                .build();
                        logger.warn("Firebase initialized without authentication - suitable for development only");
                    }
                }

                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase Admin SDK initialized successfully");
            } else {
                logger.info("Firebase Admin SDK already initialized");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    /**
     * Provide FirebaseAuth bean for dependency injection.
     * 
     * @return FirebaseAuth instance
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
