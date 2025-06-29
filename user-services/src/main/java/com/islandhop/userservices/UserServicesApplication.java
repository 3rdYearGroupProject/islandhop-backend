package com.islandhop.userservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UserServicesApplication {

    private static final Logger logger = LoggerFactory.getLogger(UserServicesApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServicesApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🚀 User Services Application is ready and running!");
        logger.info("📋 Database connection tests will be performed...");
    }
}