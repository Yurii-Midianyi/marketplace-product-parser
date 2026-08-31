package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.parser.FileNameParser;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SingleFileProcessorTest {

    @TempDir
    Path tempDir;

    private ProductExportProcessingService processingService;
    private FileMover fileMover;
    private FileNameParser fileNameParser;

    private SingleFileProcessor processor;

    @BeforeEach
    void setUp() {
        processingService = mock(ProductExportProcessingService.class);
        fileMover = mock(FileMover.class);
        fileNameParser = mock(FileNameParser.class);

        processor = new SingleFileProcessor(processingService, fileMover, fileNameParser);
    }

    @Test
    void process_validFile_returnsSuccessAndMovesToProcessed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products_PARTNER-A_2026-03-23.json"), "{\"products\":[]}");

        when(fileNameParser.parse("products_PARTNER-A_2026-03-23.json"))
                .thenReturn(new FileNameMetadata("PARTNER-A", LocalDate.of(2026, 3, 23)));
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.success("products_PARTNER-A_2026-03-23.json"));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
        assertThat(result.errors()).isEmpty();

        verify(fileMover).moveToProcessed(file);
        verify(fileMover, never()).moveToFailed(any(), anyString());
        verify(processingService).process(eq(new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, 3, 23),
                IngestionFormat.JSON,
                "products_PARTNER-A_2026-03-23.json",
                "{\"products\":[]}"
        )));
    }

    @Test
    void process_invalidFile_returnsValidationErrorAndMovesToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products_PARTNER-A_2026-03-23.csv"), "data");
        List<String> errors = List.of("name: must not be blank", "price: must be positive");

        when(fileNameParser.parse("products_PARTNER-A_2026-03-23.csv"))
                .thenReturn(new FileNameMetadata("PARTNER-A", LocalDate.of(2026, 3, 23)));
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.validationError("products_PARTNER-A_2026-03-23.csv", errors));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.csv");
        assertThat(result.errors()).containsExactlyElementsOf(errors);
        verify(fileMover).moveToFailed(eq(file), anyString());
        verify(fileMover, never()).moveToProcessed(any());
    }

    @Test
    void process_parseException_returnsParseErrorAndMovesToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products_PARTNER-A_2026-03-23.json"), "not-json");

        when(fileNameParser.parse("products_PARTNER-A_2026-03-23.json"))
                .thenReturn(new FileNameMetadata("PARTNER-A", LocalDate.of(2026, 3, 23)));
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.parseError("products_PARTNER-A_2026-03-23.json", "Unexpected end of input"));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
        assertThat(result.errors()).hasSize(1);
        verify(fileMover).moveToFailed(eq(file), anyString());
    }

    @Test
    void process_parseExceptionAndMoveToFailedAlsoThrows_returnsParseErrorWithoutPropagating() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products_PARTNER-A_2026-03-23.json"), "bad");

        when(fileNameParser.parse("products_PARTNER-A_2026-03-23.json"))
                .thenReturn(new FileNameMetadata("PARTNER-A", LocalDate.of(2026, 3, 23)));
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.parseError("products_PARTNER-A_2026-03-23.json", "parse failure"));
        doThrow(new IOException("disk full")).when(fileMover).moveToFailed(any(), anyString());

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.errors().getFirst()).contains("parse failure");
    }

    @Test
    void process_unsupportedExtension_returnsParseErrorWithoutCallingImportService() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products_PARTNER-A_2026-03-23.xml"), "<xml/>");

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        verify(processingService, never()).process(any());
        verify(fileMover).moveToFailed(eq(file), anyString());
    }

    @Test
    void process_invalidFileName_returnsParseErrorWithoutDelegatingToCore() throws IOException {
        Path file = Files.writeString(tempDir.resolve("invalid_name.json"), "{\"products\":[]}");

        when(fileNameParser.parse("invalid_name.json"))
                .thenThrow(new IllegalArgumentException("invalid file name"));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("invalid_name.json");
        assertThat(result.errors().getFirst()).contains("invalid file name");
        verify(processingService, never()).process(any());
        verify(fileMover).moveToFailed(eq(file), anyString());
    }
}
