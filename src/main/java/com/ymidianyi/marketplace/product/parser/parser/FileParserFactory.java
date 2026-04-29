package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileParserFactory {

    private final List<FileParser> fileParsers;

    public FileParserFactory(List<FileParser> fileParsers) {
        this.fileParsers = fileParsers;
    }

    public FileParser getParser(String extension){
        return fileParsers.stream()
                .filter(obj -> obj.supports(extension))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileFormatException("Extension not supported: " + extension));
    }
}
