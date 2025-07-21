package com.islandhop.adminservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for JPA repositories and transaction management.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.islandhop.adminservice.repository")
@EnableTransactionManagement
public class JpaConfig {
}
