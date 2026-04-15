package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DirectoryFileScanner implements FileScanner {

    private static final String SUPPORTED_EXTENSIONS_GLOB = "*.{json,csv}";

    @Override
    public List<Path> scan(Path directory) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, SUPPORTED_EXTENSIONS_GLOB)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    matchingFiles.add(entry);
                }
            }
        }

        matchingFiles.sort(Path::compareTo);

        log.debug("Scanned directory '{}': found {} file(s)", directory, matchingFiles.size());
        return matchingFiles;
    }
}
