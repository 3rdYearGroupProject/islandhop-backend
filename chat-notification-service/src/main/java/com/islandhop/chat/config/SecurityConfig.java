package com.islandhop.chat.config;

import com.islandhop.chat.security.FirebaseAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Chat and Notification Microservice.
 * Configures Firebase authentication and CORS settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    /**
     * Configure the security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/v1/**/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/ws/**").permitAll()  // WebSocket endpoint
                .requestMatchers("/error").permitAll()
                .requestMatchers("/").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add Firebase authentication filter
            .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure CORS to allow frontend access.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow frontend origins
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    // React development server
            "http://localhost:3001",    // Alternative React port
            "https://localhost:3000",   // HTTPS React
            "https://*.vercel.app",     // Vercel deployments
            "https://*.netlify.app",    // Netlify deployments
            "*"                         // Allow all origins (adjust for production)
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
