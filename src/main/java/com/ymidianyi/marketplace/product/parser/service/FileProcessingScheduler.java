package com.ymidianyi.marketplace.product.parser.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Component
public class FileProcessingScheduler {

    private final FileProcessingOrchestrator orchestrator;

    public FileProcessingScheduler(FileProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(cron = "${file-processing.cron}")
    public void schedule(){
        log.info("File processing was scheduled");
        orchestrator.processAllFiles();
    }

}
