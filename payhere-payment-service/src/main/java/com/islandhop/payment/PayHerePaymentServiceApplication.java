package com.islandhop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PayHere Payment Service Application
 * Main entry point for the PayHere payment integration microservice
 */
@SpringBootApplication
public class PayHerePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayHerePaymentServiceApplication.class, args);
    }
}
