package com.islandhop.adminservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-key:classpath:serviceAccountKey.json}")
    private String serviceAccountKeyPath;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initializeFirebase() {
        if (!firebaseEnabled) {
            logger.info("🔥 Firebase is disabled by configuration");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Check if service account key exists
                ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
                if (!resource.exists()) {
                    logger.warn("⚠️ Firebase service account key not found. Firebase features will be disabled.");
                    logger.warn("⚠️ To enable Firebase, place 'serviceAccountKey.json' in src/main/resources/");
                    return;
                }

                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            logger.warn("⚠️ Failed to initialize Firebase Admin SDK: {} - Firebase features will be disabled", e.getMessage());
            logger.debug("Firebase initialization error details:", e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error during Firebase initialization: {}", e.getMessage());
            logger.debug("Firebase initialization error details:", e);
        }
    }
}