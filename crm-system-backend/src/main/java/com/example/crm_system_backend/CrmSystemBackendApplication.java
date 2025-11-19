package com.example.crm_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

@SpringBootApplication
public class CrmSystemBackendApplication {


	public static void main(String[] args) {
		SpringApplication.run(CrmSystemBackendApplication.class, args);
	}

}
