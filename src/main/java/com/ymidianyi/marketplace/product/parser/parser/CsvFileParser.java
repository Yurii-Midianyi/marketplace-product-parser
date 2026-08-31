package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.CsvProductRow;
import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.CsvParsingException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.csv.CsvSchema;

@Slf4j
@Component
public class CsvFileParser implements FileParser {
    private static final CsvSchema CSV_SCHEMA = CsvSchema.builder()
            .addColumn("name")
            .addColumn("sku")
            .addColumn("price")
            .addColumn("specialPrice")
            .addColumn("specialFrom")
            .addColumn("specialTo")
            .addColumn("state")
            .addColumn("brand")
            .addColumn("category")
            .addColumn("imageUrl")
            .build()
            .withSkipFirstDataRow(true);
    private final ObjectMapper csvMapper;

    public CsvFileParser(@Qualifier("csvObjectMapper") ObjectMapper csvMapper) {
        this.csvMapper = csvMapper;
    }

    @Override
    public ProductExportFileDto parse(IncomingProductExport incomingProductExport) {
        log.debug("Parsing CSV payload from source: {}", sourceRefOf(incomingProductExport));

        List<ProductDto> products = readProducts(incomingProductExport);

        return new ProductExportFileDto(
                incomingProductExport.partnerId(),
                incomingProductExport.exportDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                products
        );
    }

    @Override
    public boolean supports(IngestionFormat format) {
        return format == IngestionFormat.CSV;
    }

    private List<ProductDto> readProducts(IncomingProductExport incomingProductExport) {
        try (MappingIterator<CsvProductRow> iterator = csvMapper
                .readerFor(CsvProductRow.class)
                .with(CSV_SCHEMA)
                .readValues(incomingProductExport.payload())) {

            return iterator.readAll().stream()
                    .map(this::toProductDto)
                    .toList();
        } catch (JacksonException e) {
            log.error("Failed to parse CSV payload {}: {}", sourceRefOf(incomingProductExport), e.getMessage());
            throw new CsvParsingException("Failed to parse CSV payload: " + sourceRefOf(incomingProductExport), e);
        }
    }

    private static String sourceRefOf(IncomingProductExport incomingProductExport) {
        return incomingProductExport.sourceRef() != null ? incomingProductExport.sourceRef() : "<unknown>";
    }

    private ProductDto toProductDto(CsvProductRow row) {
        List<String> categories = row.category() != null && !row.category().isBlank()
                ? List.of(row.category().trim())
                : List.of();

        return new ProductDto(
                row.name(),
                row.sku(),
                row.price(),
                row.specialPrice(),
                row.specialFrom(),
                row.specialTo(),
                row.state(),
                row.brand(),
                categories,
                row.imageUrl()
        );
    }
}
