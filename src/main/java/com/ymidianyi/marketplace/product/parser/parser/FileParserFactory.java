package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;

import org.springframework.stereotype.Component;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileParserFactory {

    private final List<FileParser> parsers;

    public FileParserFactory(List<FileParser> parsers) {
        this.parsers = parsers;
        log.debug("Registered file parsers: {}", parsers.stream()
                .map(p -> p.getClass().getSimpleName())
                .toList());
    }

    public FileParser getParser(IngestionFormat format) {
        return parsers.stream()
                .filter(parser -> parser.supports(format))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileFormatException(format.name().toLowerCase()));
    }
}
