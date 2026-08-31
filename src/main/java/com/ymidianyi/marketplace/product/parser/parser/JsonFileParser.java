package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;

import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JsonFileParser implements FileParser {

    private static final String SUPPORTED_EXTENSION = "json";

    private final ObjectMapper objectMapper;

    public JsonFileParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductExportFileDto parse(IncomingProductExport incomingProductExport) {
        log.debug("Parsing JSON payload from source: {}", sourceRefOf(incomingProductExport));
        try {
            ProductExportFileDto parsed = objectMapper.readValue(incomingProductExport.payload(), ProductExportFileDto.class);
            return new ProductExportFileDto(
                    incomingProductExport.partnerId(),
                    incomingProductExport.exportDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                    parsed.products()
            );
        } catch (JacksonException e) {
            log.error("Failed to parse JSON payload {}: {}", sourceRefOf(incomingProductExport), e.getMessage());
            throw new JsonParsingException("Failed to parse JSON payload: " + sourceRefOf(incomingProductExport), e);
        }
    }

    @Override
    public boolean supports(IngestionFormat format) {
        return format == IngestionFormat.JSON;
    }

    private static String sourceRefOf(IncomingProductExport incomingProductExport) {
        return incomingProductExport.sourceRef() != null ? incomingProductExport.sourceRef() : "<unknown>";
    }
}