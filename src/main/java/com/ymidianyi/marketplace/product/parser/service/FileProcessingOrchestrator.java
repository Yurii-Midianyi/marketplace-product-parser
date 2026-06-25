package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingStatus;
import com.ymidianyi.marketplace.product.parser.processor.SingleFileProcessor;
import com.ymidianyi.marketplace.product.parser.scanner.FileScanner;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileProcessingOrchestrator {
    private final SingleFileProcessor singleFileProcessor;
    private final FileScanner fileScanner;
    private final ExecutorService executorService;
    private final FileProcessingProperties properties;

    public FileProcessingOrchestrator(SingleFileProcessor singleFileProcessor,
                                      FileScanner fileScanner,
                                      ExecutorService executorService,
                                      FileProcessingProperties properties) {
        this.singleFileProcessor = singleFileProcessor;
        this.fileScanner = fileScanner;
        this.executorService = executorService;
        this.properties = properties;
    }

    public void processAllFiles() {
        List<Path> files = fileScanner.scan(Path.of(properties.getInputDir()));
        if(files.isEmpty()) {
            log.info("No files to process");
            return;
        }
        log.info("Found {} files to process", files.size());
        int timeoutMinutes = properties.getFileProcessingTimeoutMinutes();

        /*
        Process all the files asynchronously. Amount of threads is equal to threadPoolSize from FileProcessingProperties.
        If a file is processed longer than timeoutMinutes it is considered failed.
        */
        List<CompletableFuture<ProcessingResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(()->singleFileProcessor.process(file), executorService)
                        .orTimeout(timeoutMinutes, TimeUnit.MINUTES)
                        .exceptionally(ex->{
                            log.error("File {} failed to process after {} minutes", file, timeoutMinutes);
                            return ProcessingResult.parseError(file.getFileName().toString(), "Failed to be processed after " + timeoutMinutes + " minutes");
                        }))
                .toList();

        //wait for all threads to complete. Convert to array because .allOf method works only with arrays
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]::new)).join();

        // All futures are guaranteed complete at this point, so join() on each one
        // returns immediately without blocking.
        List<ProcessingResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        logSummary(results);
    }

    private void logSummary(List<ProcessingResult> results) {
        long successCount = results.stream()
                .filter(r->r.status() == ProcessingStatus.SUCCESS)
                .count();
        long failedCount = results.size() - successCount;
        log.info("Total files processed: {}, files processed successfully: {}, files failed: {}", results.size(), successCount, failedCount);
    }
}
