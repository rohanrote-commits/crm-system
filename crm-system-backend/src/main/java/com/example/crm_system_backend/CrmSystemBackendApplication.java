package com.example.crm_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrmSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmSystemBackendApplication.class, args);
    }

}
