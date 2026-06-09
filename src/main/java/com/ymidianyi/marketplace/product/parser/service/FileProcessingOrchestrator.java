package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingStatus;
import com.ymidianyi.marketplace.product.parser.processor.SingleFileProcessor;
import com.ymidianyi.marketplace.product.parser.scanner.FileScanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class FileProcessingOrchestrator {

    private final FileScanner fileScanner;
    private final SingleFileProcessor fileProcessor;
    private final ExecutorService executorService;
    private final FileProcessingProperties properties;

    public FileProcessingOrchestrator(FileScanner fileScanner,
                                      SingleFileProcessor fileProcessor,
                                      ExecutorService executorService,
                                      FileProcessingProperties properties) {
        this.fileScanner = fileScanner;
        this.fileProcessor = fileProcessor;
        this.executorService = executorService;
        this.properties = properties;
    }

    public void processAllFiles() throws IOException {
        List<Path> files = fileScanner.scan(Path.of(properties.getInputDir()));
        if (files.isEmpty()) {
            log.info("No files found in input directory.");
            return;
        }
        log.info("Found {} file(s) to process.", files.size());

        // submit every file to the thread pool at once.
        // supplyAsync schedules fileProcessor.process(file) on a worker thread and
        // immediately returns a CompletableFuture — a handle to the result that will
        // arrive later. All files start in parallel; this line does not wait for any of them.
        List<CompletableFuture<ProcessingResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> fileProcessor.process(file), executorService))
                .toList();

        // block the current thread until every future has completed.
        // allOf() creates a single combined future that is done only when all
        // individual futures are done. join() then waits for that combined future.
        // Because SingleFileProcessor.process() never throws, every future completes
        // normally — so join() will not throw a CompletionException here.
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join(); // try to remove and see the result

        // All futures are guaranteed complete at this point, so join() on each one
        // returns immediately without blocking.
        List<ProcessingResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        logSummary(results);
    }

    private void logSummary(List<ProcessingResult> results) {
        long successCount = results.stream()
                .filter(r -> r.status() == ProcessingStatus.SUCCESS)
                .count();
        long failedCount = results.size() - successCount;

        log.info("Processing complete — total: {}, success: {}, failed: {}",
                results.size(), successCount, failedCount);

        results.stream()
                .filter(r -> r.status() != ProcessingStatus.SUCCESS)
                .forEach(r -> log.warn("  [{}] '{}': {}", r.status(), r.fileName(), r.errors()));
    }
}
