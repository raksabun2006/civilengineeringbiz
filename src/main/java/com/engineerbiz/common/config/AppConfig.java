package com.engineerbiz.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

/**
 * General application configuration establishing system default timezone,
 * internationalization defaults, and Jackson serialization.
 */
@Configuration
public class AppConfig {

    public static final String PHNOM_PENH_TIMEZONE = "Asia/Phnom_Penh";

    @PostConstruct
    public void init() {
        // Enforce system default timezone as UTC for database interactions and internal processing.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}
