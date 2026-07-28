package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class ExecutorConfig {
    private ExecutorService executorService;

    @Bean
    public ExecutorService executorService(FileProcessingProperties properties) {
        this.executorService = Executors.newFixedThreadPool(properties.getThreadPoolSize());
        log.info("Executor service started with pool size of {}", properties.getThreadPoolSize());
        return executorService;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if(executorService == null) {
            return;
        }
        log.info("Executor service shutting down");
        executorService.shutdown();
        if(!executorService.awaitTermination(30, TimeUnit.SECONDS)){
            log.error("Executor service did not terminate within 30 seconds, forcing shutdown");
            executorService.shutdownNow();
        }
        log.info("Executor service shut down");
    }
}
