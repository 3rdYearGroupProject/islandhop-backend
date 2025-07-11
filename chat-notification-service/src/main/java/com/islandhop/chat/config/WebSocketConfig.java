package com.islandhop.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time messaging.
 * Configures STOMP endpoints and message broker for chat functionality.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${chat.websocket.endpoint:/ws-chat}")
    private String websocketEndpoint;

    @Value("${chat.websocket.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    /**
     * Configure message broker for WebSocket messaging.
     * Sets up topic and queue prefixes for different message types.
     * 
     * @param config MessageBrokerRegistry configuration
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple message broker for topics and queues
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     * Configures endpoint URL and CORS settings.
     * 
     * @param registry StompEndpointRegistry for endpoint registration
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint(websocketEndpoint)
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
        
        // Register raw WebSocket endpoint (without SockJS)
        registry.addEndpoint(websocketEndpoint)
                .setAllowedOrigins(allowedOrigins);
    }
}
