package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;

import org.springframework.stereotype.Component;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileParserFactory {

    private final List<FileParser> fileParsers;

    public FileParserFactory(List<FileParser> fileParsers) {
        this.fileParsers = fileParsers;
        log.debug("Registered file parsers : {}", fileParsers.stream()
                .map(p->p.getClass().getSimpleName())
                .toList());
    }

    public FileParser getParser(IngestionFormat format){
        return fileParsers.stream()
                .filter(obj -> obj.supports(format))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileFormatException("Extension not supported: " + format.name().toLowerCase()));
    }
}
