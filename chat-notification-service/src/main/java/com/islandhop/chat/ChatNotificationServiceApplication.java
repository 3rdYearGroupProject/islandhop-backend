package com.islandhop.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main application class for the Chat and Notification Service.
 * This microservice handles real-time messaging and notifications for the IslandHop tourism platform.
 * 
 * Supports communication among 5 user types: admin, support, driver, guide, and tourist.
 * Uses MongoDB for chat messages, PostgreSQL for notifications, and Redis for WebSocket pub/sub.
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.islandhop.chat.repository.mongo")
@EnableJpaRepositories(basePackages = "com.islandhop.chat.repository.jpa")
public class ChatNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatNotificationServiceApplication.class, args);
    }
}
