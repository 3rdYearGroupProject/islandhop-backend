package com.islandhop.emergencyservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmergencyServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmergencyServicesApplication.class, args);
    }
} 