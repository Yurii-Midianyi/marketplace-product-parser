package com.ymidianyi.marketplace.product.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class FileProcessingScheduler {

    private final FileProcessingOrchestrator orchestrator;

    public FileProcessingScheduler(FileProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(cron = "${file-processing.cron}")
    public void run() {
        log.info("Scheduled file processing triggered");
        try {
            orchestrator.processAllFiles();
        } catch (IOException e) {
            log.error("File processing failed: {}", e.getMessage(), e);
        }
    }
}
