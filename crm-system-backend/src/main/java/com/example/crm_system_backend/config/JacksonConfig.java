package com.example.crm_system_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    /**
     * Defines a Spring bean that provides a customized {@link ObjectMapper} instance.
     * The configured ObjectMapper includes support for Java Time API objects
     * through the addition of the {@link JavaTimeModule} and is set to avoid
     * serializing dates as timestamps by disabling the {@code WRITE_DATES_AS_TIMESTAMPS} feature.
     *
     * @return an {@link ObjectMapper} instance configured with custom settings
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}