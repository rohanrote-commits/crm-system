package com.example.crm_system_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Defines a Spring bean that provides a ThreadPoolTaskExecutor for asynchronous processing.
     * This executor is configured with specific properties, including a core pool size of 1,
     * maximum pool size of 1, a queue capacity of 10, and a custom thread name prefix "BulkUpload-".
     * It is primarily used for handling bulk upload-related tasks in an asynchronous manner.
     *
     * @return an Executor instance configured as a ThreadPoolTaskExecutor
     */
    @Bean(name = "bulkUploadExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("BulkUpload-");
        executor.initialize();
        return executor;
    }
}
