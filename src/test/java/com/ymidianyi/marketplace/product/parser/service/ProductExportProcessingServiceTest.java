package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingStatus;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductExportProcessingServiceTest {

    private FileParserFactory parserFactory;
    private FileParser fileParser;
    private ProductExportValidator validator;
    private ProductImportService importService;
    private ProductExportProcessingService processingService;

    @BeforeEach
    void setUp() {
        parserFactory = mock(FileParserFactory.class);
        fileParser = mock(FileParser.class);
        validator = mock(ProductExportValidator.class);
        importService = mock(ProductImportService.class);
        processingService = new ProductExportProcessingService(parserFactory, validator, importService);
    }

    @Test
    void process_validIncomingExport_returnsSuccessAndImportsProducts() {
        IncomingProductExport incoming = incomingExport();
        ProductExportFileDto dto = validDto();

        when(parserFactory.getParser(IngestionFormat.JSON)).thenReturn(fileParser);
        when(fileParser.parse(incoming)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.ok());

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
        verify(importService).importProducts(dto, "products_PARTNER-A_2026-03-23.json");
    }

    @Test
    void process_validCsvIncomingExport_routesThroughCsvParserAndImportsProducts() {
        IncomingProductExport incoming = new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, Month.MARCH, 23),
                IngestionFormat.CSV,
                "products_PARTNER-A_2026-03-23.csv",
                "name,sku,price,state\nApple,SKU-1,10.0,ACTIVE\n"
        );
        ProductExportFileDto dto = validDto();

        when(parserFactory.getParser(IngestionFormat.CSV)).thenReturn(fileParser);
        when(fileParser.parse(incoming)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.ok());

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.csv");
        verify(importService).importProducts(dto, "products_PARTNER-A_2026-03-23.csv");
    }

    @Test
    void process_invalidIncomingExport_returnsValidationErrorWithoutImporting() {
        IncomingProductExport incoming = incomingExport();
        ProductExportFileDto dto = validDto();
        List<String> errors = List.of("sku: must not be blank");

        when(parserFactory.getParser(IngestionFormat.JSON)).thenReturn(fileParser);
        when(fileParser.parse(incoming)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.invalid(errors));

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.errors()).containsExactlyElementsOf(errors);
        verify(importService, never()).importProducts(any(), anyString());
    }

    @Test
    void process_parseFailure_returnsParseErrorWithoutThrowing() {
        IncomingProductExport incoming = incomingExport();

        when(parserFactory.getParser(IngestionFormat.JSON)).thenReturn(fileParser);
        when(fileParser.parse(incoming)).thenThrow(new JsonParsingException("bad json", null));

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
        assertThat(result.errors().getFirst()).contains("bad json");
        verify(importService, never()).importProducts(any(), anyString());
    }

    @Test
    void process_importFailure_returnsParseErrorUsingFallbackSourceRef() {
        IncomingProductExport incoming = new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, Month.MARCH, 23),
                IngestionFormat.JSON,
                null,
                "{\"products\":[]}"
        );
        ProductExportFileDto dto = validDto();

        when(parserFactory.getParser(IngestionFormat.JSON)).thenReturn(fileParser);
        when(fileParser.parse(incoming)).thenReturn(dto);
        when(validator.validate(dto)).thenReturn(ValidationResult.ok());
        doThrow(new IllegalStateException("db unavailable")).when(importService).importProducts(dto, "PARTNER-A:2026-03-23");

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("PARTNER-A:2026-03-23");
        assertThat(result.errors().getFirst()).contains("db unavailable");
    }

    @Test
    void process_parserSelectionFailure_returnsParseErrorWithoutImporting() {
        IncomingProductExport incoming = incomingExport();

        when(parserFactory.getParser(IngestionFormat.JSON))
                .thenThrow(new IllegalStateException("no parser"));

        ProcessingResult result = processingService.process(incoming);

        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.fileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
        assertThat(result.errors().getFirst()).contains("no parser");
        verify(importService, never()).importProducts(any(), anyString());
    }

    private IncomingProductExport incomingExport() {
        return new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, Month.MARCH, 23),
                IngestionFormat.JSON,
                "products_PARTNER-A_2026-03-23.json",
                "{\"products\":[]}"
        );
    }

    private ProductExportFileDto validDto() {
        ProductDto product = new ProductDto(
                "Apple Fruit", "SKU-001", BigDecimal.valueOf(100), null,
                null, null, ProductState.ACTIVE, null, List.of(), null);
        return new ProductExportFileDto("PARTNER-A", Instant.now(), List.of(product));
    }
}
