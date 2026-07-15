package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.SingleFileProcessor;
import com.ymidianyi.marketplace.product.parser.scanner.FileScanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileProcessingOrchestratorTest {

    @TempDir
    Path tempDir;

    private FileScanner fileScanner;
    private SingleFileProcessor fileProcessor;
    private FileProcessingProperties properties;
    private ExecutorService executorService;
    private FileProcessingOrchestrator orchestrator;

    @BeforeEach
    public void setup() {
        fileScanner = mock(FileScanner.class);
        fileProcessor = mock(SingleFileProcessor.class);
        properties = mock(FileProcessingProperties.class);
        executorService = Executors.newFixedThreadPool(2);

        when(properties.getInputDir()).thenReturn(tempDir.toString());
        when(properties.getFileProcessingTimeoutMinutes()).thenReturn(1);

        orchestrator = new FileProcessingOrchestrator(fileProcessor,fileScanner, executorService, properties);
    }

    @AfterEach
    void teardown() {
        executorService.shutdown();
    }

    @Test
    void testDoNotCallProcessorIfNoFilesFound(){
        when(fileScanner.scan(any())).thenReturn(List.of());
        orchestrator.processAllFiles();
        verify(fileProcessor, never()).process(any());
    }

    @Test
    void testProcessSuccessfulFiles(){
        List<Path> files = List.of(
                tempDir.resolve("a.json"),
                tempDir.resolve("b.json"),
                tempDir.resolve("c.csv")
        );
        when(fileScanner.scan(any())).thenReturn(files);
        when(fileProcessor.process(any())).thenReturn(ProcessingResult.success("any"));

        orchestrator.processAllFiles();
        verify(fileProcessor, times(3)).process(any());
    }

    @Test
    void testContinueProcessingFilesEvenWhenOneReturnsParseError(){
        List<Path> files = List.of(
                tempDir.resolve("good.json"),
                tempDir.resolve("bad.json"),
                tempDir.resolve("c.csv")
        );
        when(fileScanner.scan(any())).thenReturn(files);
        when(fileProcessor.process(any())).thenReturn(ProcessingResult.success("any"));
        when(fileProcessor.process(argThat(path->path.getFileName().toString().equals("bad.json"))))
                .thenReturn(ProcessingResult.parseError("bad.json", "Unexpected error"));

        assertThatCode(()->orchestrator.processAllFiles()).doesNotThrowAnyException();
        verify(fileProcessor, times(3)).process(any());

    }

    @Test
    void testProcessAllFileWithMixedResults(){
        List<Path> files = List.of(
                tempDir.resolve("first.json"),
                tempDir.resolve("bad.json"),
                tempDir.resolve("third.csv"),
                tempDir.resolve("fourth.csv"),
                tempDir.resolve("bad2.csv")
        );
        when(fileScanner.scan(any())).thenReturn(files);
        when(fileProcessor.process(any())).thenReturn(ProcessingResult.success("any"));
        when(fileProcessor.process(files.get(1)))
                .thenReturn(ProcessingResult.validationError("bad.json", List.of("Duplicate name")));
        when(fileProcessor.process(files.get(4)))
                .thenReturn(ProcessingResult.validationError("bad2.csv", List.of("Price can not be positive")));
        assertThatCode(()->orchestrator.processAllFiles()).doesNotThrowAnyException();
        verify(fileProcessor, times(5)).process(any());
    }
}
