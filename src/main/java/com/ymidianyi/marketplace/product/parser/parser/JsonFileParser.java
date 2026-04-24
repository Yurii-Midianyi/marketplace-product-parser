package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;

@Component
public class JsonFileParser implements FileParser {

    private final ObjectMapper objectMapper;

    public JsonFileParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductExportFileDto parse(Path file) throws IOException {
//        String content = Files.readString(file);
//        return objectMapper.readValue(content, ProductExportFileDto.class);
          return objectMapper.readValue(file, ProductExportFileDto.class);
    }

    @Override
    public boolean supports(String fileExtension) {
        return fileExtension.equalsIgnoreCase("json");
    }
}
