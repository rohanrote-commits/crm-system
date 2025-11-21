package com.example.crm_system_backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    /**
     * Defines a Spring bean that provides an instance of {@link ModelMapper}.
     * ModelMapper is a library used for object mapping, particularly for transforming
     * data objects between layers in an application.
     *
     * @return a configured {@link ModelMapper} instance
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
