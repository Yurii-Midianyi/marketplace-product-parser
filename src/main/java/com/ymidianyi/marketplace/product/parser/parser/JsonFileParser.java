package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
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
    public ProductExportFileDto parse(Path file) throws IOException {
        log.debug("Parsing JSON file: {}", file.getFileName());
        String content = Files.readString(file);
        return objectMapper.readValue(content, ProductExportFileDto.class);
    }

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSION.equalsIgnoreCase(fileExtension);
    }
}
