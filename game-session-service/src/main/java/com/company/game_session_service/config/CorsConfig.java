package com.company.game_session_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Match all endpoints
                    .allowedOrigins("http://localhost:3000") // Allow your Frontend Microservice
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                    .allowedHeaders("*") // Allow all headers (like Content-Type)
                    .allowCredentials(true) // Allow cookies/auth headers if needed later
                    .maxAge(3600); // Cache the preflight response for 1 hour (reduces log noise)
            }
        };
    }
}
