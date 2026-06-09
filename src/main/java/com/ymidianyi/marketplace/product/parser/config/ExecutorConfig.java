package com.ymidianyi.marketplace.product.parser.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class ExecutorConfig {

    private ExecutorService executor;

    @Bean
    public ExecutorService fileProcessingExecutor(FileProcessingProperties properties) {
        // Use Executors.newVirtualThreadPerTaskExecutor()
        // to use virtual threads instead of a fixed OS-thread pool. Virtual threads are
        // cheap enough that the JVM creates one per task on demand — no pool size needed.
        // The rest of the code does not need to be changed.
        executor = Executors.newFixedThreadPool(properties.getThreadPoolSize());
        log.info("File processing executor created with pool size {}", properties.getThreadPoolSize());
        return executor;
    }

    /**
     * Using @PreDestroy here instead of
     * @Bean(destroyMethod = "shutdown") because the built-in destroyMethod only calls
     * shutdown() — it cannot also call awaitTermination(). Without awaitTermination(),
     * threads that are in process when the app stops are silently killed, which can
     * leave the database in a partial state. The two-step sequence here gives running
     * tasks up to 30 seconds to finish cleanly before forcing them to stop.
     */
    @PreDestroy
    void shutdown() throws InterruptedException {
        if (executor == null) {
            return;
        }
        log.info("Shutting down file processing executor...");
        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            log.warn("Executor did not terminate within 30 seconds, forcing shutdown.");
            executor.shutdownNow();
        }
        log.info("File processing executor shut down.");
    }
}
