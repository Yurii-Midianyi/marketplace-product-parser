package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public ProductExportFileDto parse(Path file) {
        log.debug("Parsing JSON file: {}", file.getFileName());
        try {
            String content = Files.readString(file);
            return objectMapper.readValue(content, ProductExportFileDto.class);
        } catch (IOException | JacksonException e) {
            log.error("Failed to parse JSON file {}: {}", file.getFileName(), e.getMessage());
            throw new JsonParsingException("Failed to parse JSON file: " + file.getFileName(), e);
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSION.equalsIgnoreCase(fileExtension);
    }
}