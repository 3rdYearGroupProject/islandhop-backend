package com.islandhop.adminservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AdminServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🚀 Admin Service Application is ready and running!");
        logger.info("📋 System status monitoring will be performed...");
    }
}
