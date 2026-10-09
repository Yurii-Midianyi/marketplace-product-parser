package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.parser.FileNameParser;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SingleFileProcessorTest {

    @TempDir
    Path tempDir;

    private ProductExportProcessingService processingService;
    private FileNameParser fileNameParser;
    private FileMover fileMover;
    private SingleFileProcessor singleFileProcessor;

    @BeforeEach
    void setUp(){
        processingService = mock(ProductExportProcessingService.class);
        fileNameParser = mock(FileNameParser.class);
        fileMover = mock(FileMover.class);
        singleFileProcessor = new SingleFileProcessor(processingService, fileMover, fileNameParser);
    }

    @Test
    void testProcessSuccessfulFile() throws IOException {
        Path file = Files.writeString(tempDir.resolve("product.json"), "{payload}");
        ProductExportFileDto dto = TestUtilities.createValidProductExportFileDto();
        when(fileNameParser.parseFileName(any())).thenReturn(new FileNameMetadata(dto.partnerId(), LocalDate.ofInstant(dto.exportDate(), ZoneOffset.UTC)));
        when(processingService.process(any())).thenReturn(ProcessingResult.success("product.json"));

        ProcessingResult result = singleFileProcessor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("product.json");
        assertThat(result.errors()).isEmpty();
        verify(fileMover).moveToProcessed(file);
        verify(fileMover, never()).moveToFailed(any(), anyString());
        ArgumentCaptor<IncomingProductExport> captor = ArgumentCaptor.forClass(IncomingProductExport.class);
        verify(processingService).process(captor.capture());
        IncomingProductExport capturedExport = captor.getValue();
        assertThat(capturedExport.partnerId()).isEqualTo("PARTNER-A");
        assertThat(capturedExport.payload()).isEqualTo("{payload}");
    }

    @Test
    void testProcessFailedFile() throws IOException {
        Path file = Files.writeString(tempDir.resolve("product.json"), "{}");
        ProductExportFileDto dto = TestUtilities.createValidProductExportFileDto();
        List<String> errors = List.of("name: must not be blank", "price: must be positive");

        when(fileNameParser.parseFileName(any())).thenReturn(new FileNameMetadata(dto.partnerId(), LocalDate.ofInstant(dto.exportDate(), ZoneOffset.UTC)));
        when(processingService.process(any())).thenReturn(ProcessingResult.validationError("product.json",errors));

        ProcessingResult result = singleFileProcessor.process(file);
        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.fileName()).isEqualTo("product.json");
        assertThat(result.errors()).containsExactlyElementsOf(errors);
        verify(fileMover).moveToFailed(any(), anyString());
        verify(fileMover, never()).moveToProcessed(file);
    }

    @Test
    void testFileWithParseErrorMovedToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("corrupt.json"), "wrong input");
        when(processingService.process(any())).thenReturn(ProcessingResult.parseError("product.json", "Wrong Json format"));

        ProcessingResult result = singleFileProcessor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("corrupt.json");
        assertThat(result.errors()).hasSize(1);
        verify(fileMover).moveToFailed(any(), anyString());
        verify(fileMover, never()).moveToProcessed(any());
    }

    @Test
    void testProcessUnsupportedExtension() throws IOException {
        Path file = Files.writeString(tempDir.resolve("catalog.xml"), "<xml/>");
        when(fileNameParser.parseFileName(any())).thenReturn(new FileNameMetadata(anyString(), any()));
        ProcessingResult result = singleFileProcessor.process(file);
        assertThat(result.errors())
                .anyMatch(err -> err.contains("UnsupportedFileFormatException"));

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        verify(fileMover, never()).moveToProcessed(any());
    }
}

