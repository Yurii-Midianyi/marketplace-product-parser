package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SingleFileProcessorTest {

    @TempDir
    Path tempDir;

    private FileParserFactory parserFactory;
    private FileParser fileParser;
    private ProductExportValidator validator;
    private ProductImportService importService;
    private FileMover fileMover;

    private SingleFileProcessor processor;

    @BeforeEach
    void setUp() {
        parserFactory = mock(FileParserFactory.class);
        fileParser = mock(FileParser.class);
        validator = mock(ProductExportValidator.class);
        importService = mock(ProductImportService.class);
        fileMover = mock(FileMover.class);

        processor = new SingleFileProcessor(parserFactory, validator, importService, fileMover);
    }

    @Test
    void process_validFile_returnsSuccessAndMovesToProcessed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("products.json"), "{}");
        ProductExportFileDto dto = validDto();

        when(parserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.ok());

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("products.json");
        assertThat(result.errors()).isEmpty();

        verify(importService).importProducts(dto, "products.json");
        verify(fileMover).moveToProcessed(file);
        verify(fileMover, never()).moveToFailed(any(), anyString());
    }

    @Test
    void process_invalidFile_returnsValidationErrorAndMovesToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("bad.csv"), "data");
        ProductExportFileDto dto = validDto();
        List<String> errors = List.of("name: must not be blank", "price: must be positive");

        when(parserFactory.getParser("csv")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.invalid(errors));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.fileName()).isEqualTo("bad.csv");
        assertThat(result.errors()).containsExactlyElementsOf(errors);

        verify(fileMover).moveToFailed(eq(file), anyString());
        verify(importService, never()).importProducts(any(), anyString());
        verify(fileMover, never()).moveToProcessed(any());
    }

    @Test
    void process_parseException_returnsParseErrorAndMovesToFailed() throws IOException {
        Path file = Files.writeString(tempDir.resolve("corrupt.json"), "not-json");

        when(parserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenThrow(new JsonParsingException("Unexpected end of input", null));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("corrupt.json");
        assertThat(result.errors()).hasSize(1);

        verify(fileMover).moveToFailed(eq(file), anyString());
        verify(importService, never()).importProducts(any(), anyString());
    }

    @Test
    void process_parseExceptionAndMoveToFailedAlsoThrows_returnsParseErrorWithoutPropagating() throws IOException {
        Path file = Files.writeString(tempDir.resolve("corrupt.json"), "bad");

        when(parserFactory.getParser("json")).thenReturn(fileParser);
        when(fileParser.parse(file)).thenThrow(new JsonParsingException("parse failure", null));
        doThrow(new IOException("disk full")).when(fileMover).moveToFailed(any(), anyString());

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.errors().getFirst()).contains("parse failure");
    }

    @Test
    void process_unsupportedExtension_returnsParseErrorWithoutCallingImportService() throws IOException {
        Path file = Files.writeString(tempDir.resolve("catalog.xml"), "<xml/>");

        when(parserFactory.getParser("xml"))
                .thenThrow(new com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException("xml"));

        ProcessingResult result = processor.process(file);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        verify(importService, never()).importProducts(any(), anyString());
    }

    private ProductExportFileDto validDto() {
        ProductDto product = new ProductDto(
                "Apple Fruit", "SKU-001", BigDecimal.valueOf(100), null,
                null, null, ProductState.ACTIVE, null, List.of(), null);
        return new ProductExportFileDto("PARTNER-A", Instant.now(), List.of(product));
    }
}
