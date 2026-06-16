package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.SingleFileProcessor;
import com.ymidianyi.marketplace.product.parser.scanner.FileScanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileProcessingOrchestratorTest {

    @TempDir
    Path tempDir;

    private FileScanner fileScanner;
    private SingleFileProcessor fileProcessor;
    private FileProcessingProperties properties;
    private ExecutorService executorService;

    private FileProcessingOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        fileScanner = mock(FileScanner.class);
        fileProcessor = mock(SingleFileProcessor.class);
        properties = mock(FileProcessingProperties.class);
        executorService = Executors.newFixedThreadPool(2);

        when(properties.getInputDir()).thenReturn(tempDir.toString());
        when(properties.getFileProcessingTimeoutMinutes()).thenReturn(5);

        orchestrator = new FileProcessingOrchestrator(fileScanner, fileProcessor, executorService, properties);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void processAllFiles_noFilesFound_doesNotCallProcessor() throws IOException {
        when(fileScanner.scan(any())).thenReturn(List.of());

        orchestrator.processAllFiles();

        verify(fileProcessor, never()).process(any());
    }

    @Test
    void processAllFiles_threeFiles_allAreProcessed() throws IOException {
        List<Path> files = List.of(
                tempDir.resolve("a.json"),
                tempDir.resolve("b.csv"),
                tempDir.resolve("c.json")
        );
        when(fileScanner.scan(any())).thenReturn(files);
        when(fileProcessor.process(any())).thenReturn(ProcessingResult.success("any"));

        orchestrator.processAllFiles();

        verify(fileProcessor, times(3)).process(any());
    }

    @Test
    void processAllFiles_oneFileFails_otherFilesAreStillProcessed() throws IOException {
        Path goodFile = tempDir.resolve("good.json");
        Path badFile  = tempDir.resolve("bad.json");

        when(fileScanner.scan(any())).thenReturn(List.of(goodFile, badFile));
        when(fileProcessor.process(goodFile)).thenReturn(ProcessingResult.success("good.json"));
        when(fileProcessor.process(badFile))
                .thenReturn(ProcessingResult.parseError("bad.json", "Unexpected end of JSON"));

        assertThatCode(() -> orchestrator.processAllFiles()).doesNotThrowAnyException();

        verify(fileProcessor).process(goodFile);
        verify(fileProcessor).process(badFile);
    }

    @Test
    void processAllFiles_mixedResults_doesNotThrow() throws IOException {
        List<Path> files = List.of(
                tempDir.resolve("ok1.json"),
                tempDir.resolve("ok2.json"),
                tempDir.resolve("ok3.json"),
                tempDir.resolve("fail1.csv"),
                tempDir.resolve("fail2.csv")
        );
        when(fileScanner.scan(any())).thenReturn(files);

        when(fileProcessor.process(files.get(0))).thenReturn(ProcessingResult.success("ok1.json"));
        when(fileProcessor.process(files.get(1))).thenReturn(ProcessingResult.success("ok2.json"));
        when(fileProcessor.process(files.get(2))).thenReturn(ProcessingResult.success("ok3.json"));
        when(fileProcessor.process(files.get(3)))
                .thenReturn(ProcessingResult.validationError("fail1.csv", List.of("sku: must not be blank")));
        when(fileProcessor.process(files.get(4)))
                .thenReturn(ProcessingResult.parseError("fail2.csv", "Invalid date format"));

        assertThatCode(() -> orchestrator.processAllFiles()).doesNotThrowAnyException();

        verify(fileProcessor, times(5)).process(any());
    }
}
