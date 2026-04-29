package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.CsvProductRow;
import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.CsvParsingException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
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

    private static final String SUPPORTED_EXTENSION = "csv";

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
    private final FileNameParser fileNameParser;

    public CsvFileParser(@Qualifier("csvObjectMapper") ObjectMapper csvMapper,
                         FileNameParser fileNameParser) {
        this.csvMapper = csvMapper;
        this.fileNameParser = fileNameParser;
    }

    @Override
    public ProductExportFileDto parse(Path file) {
        log.debug("Parsing CSV file: {}", file.getFileName());

        FileNameMetadata metadata = fileNameParser.parse(file.getFileName().toString());

        List<ProductDto> products = readProducts(file);

        return new ProductExportFileDto(
                metadata.partnerId(),
                metadata.exportDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                products
        );
    }

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSION.equalsIgnoreCase(fileExtension);
    }

    private List<ProductDto> readProducts(Path file) {
        try (MappingIterator<CsvProductRow> iterator = csvMapper
                .readerFor(CsvProductRow.class)
                .with(CSV_SCHEMA)
                .readValues(file.toFile())) {

            return iterator.readAll().stream()
                    .map(this::toProductDto)
                    .toList();
        } catch (JacksonException e) {
            log.error("Failed to parse CSV file {}: {}", file.getFileName(), e.getMessage());
            throw new CsvParsingException("Failed to parse CSV file: " + file.getFileName(), e);
        }
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
