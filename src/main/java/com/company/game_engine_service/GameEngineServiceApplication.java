package com.company.game_engine_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class GameEngineServiceApplication {

    @Configuration
    static class TestConfig {
	@Bean
	    public ObjectMapper getObjectMapper() {
	    return new ObjectMapper();
	}
    }

    public static void main(String[] args) {
		SpringApplication.run(GameEngineServiceApplication.class, args);
	}
}
