package com.ymidianyi.marketplace.product.parser.scanner;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefaultFileMover implements FileMover {

    private final FileProcessingProperties properties;
    private static final String ERROR_FILE_SUFFIX = ".error";

    public DefaultFileMover(FileProcessingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void moveToProcessed(Path file) {
        if(Files.isDirectory(file)){
            throw new IllegalArgumentException(file.toString() + " is not a file");
        }
        try {
            Path target = Path.of(properties.getProcessedDir()).resolve(file.getFileName());
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.info("{} failed to be moved", file);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void moveToFailed(Path file, String errorMessage) {
        if(Files.isDirectory(file)){
            throw new IllegalArgumentException(file.toString() + " is not a file");
        }
        try {
            Path target = Path.of(properties.getFailedDir()).resolve(file.getFileName());
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            Path errorFile = target.resolveSibling(file.getFileName() + ERROR_FILE_SUFFIX);
            Files.writeString(errorFile, errorMessage);
        } catch (IOException e) {
            log.info("{} failed to be moved", file);
            throw new RuntimeException(e);
        }
    }
}
