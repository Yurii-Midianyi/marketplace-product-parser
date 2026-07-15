package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;
import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductImportService;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SingleFileProcessorTest {

    @TempDir
    Path tempDir;

    private FileParserFactory fileParserFactory;
    private FileParser fileParser;
    private ProductExportValidator validator;
    private ProductImportService importService;
    private FileMover fileMover;
    private SingleFileProcessor singleFileProcessor;

    @BeforeEach
    void setUp(){
        fileParserFactory = mock(FileParserFactory.class);
        fileParser = mock(FileParser.class);
        validator = mock(ProductExportValidator.class);
        importService = mock(ProductImportService.class);
        fileMover = mock(FileMover.class);
        singleFileProcessor = new SingleFileProcessor(fileParserFactory, validator, importService, fileMover);
    }

    @Test
    void testProcessSuccessfulFile() throws IOException {
        Path file = Files.writeString(tempDir.resolve("product.json"), "{}");
        ProductExportFileDto dto = TestUtilities.createValidProductExportFileDto();
        when(fileParserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.ok());

        ProcessingResult result = singleFileProcessor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("product.json");
        assertThat(result.errors()).isEmpty();
        verify(importService).importProducts(dto, "product.json");
        verify(fileMover).moveToProcessed(file);
        verify(fileMover, never()).moveToFailed(any(), anyString());
    }

    @Test
    void testProcessFailedFile() throws IOException {
        Path file = Files.writeString(tempDir.resolve("product.json"), "{}");
        ProductExportFileDto dto = TestUtilities.createValidProductExportFileDto();
        List<String> errors = List.of("name: must not be blank", "price: must be positive");

        when(fileParserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.invalid(errors));
        ProcessingResult result = singleFileProcessor.process(file);
        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.fileName()).isEqualTo("product.json");
        assertThat(result.errors()).containsExactlyElementsOf(errors);
        verify(fileMover).moveToFailed(any(), anyString());
        verify(importService, never()).importProducts(any(), anyString());
        verify(fileMover, never()).moveToProcessed(file);
    }

    @Test
    void testFileWithParseErrorMovedToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("corrupt.json"), "wrong input");
        when(fileParserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenThrow(new JsonParsingException("Wrong Json format", null));
        ProcessingResult result = singleFileProcessor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("corrupt.json");
        assertThat(result.errors()).hasSize(1);
        verify(fileMover).moveToFailed(any(), anyString());
        verify(importService, never()).importProducts(any(), anyString());
    }

    @Test
    void testParseErrorDoesNotPropagateAnException() throws IOException {
        Path file = Files.writeString(tempDir.resolve("corrupt.json"), "wrong input");
        when(fileParserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenThrow(new JsonParsingException("Wrong Json format", null));
        doThrow(new IOException("unexpected error")).when(fileMover).moveToFailed(any(), anyString());

        ProcessingResult result = singleFileProcessor.process(file);
        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.errors().getFirst()).contains("Wrong Json format");
    }

    @Test
    void testProcessUnsupportedExtension() throws IOException {
        Path file = Files.writeString(tempDir.resolve("catalog.xml"), "<xml/>");
        when(fileParserFactory.getParser("xml")).thenThrow(new UnsupportedFileFormatException("xml"));

        ProcessingResult result = singleFileProcessor.process(file);
        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        verify(importService, never()).importProducts(any(), anyString());
    }
}

