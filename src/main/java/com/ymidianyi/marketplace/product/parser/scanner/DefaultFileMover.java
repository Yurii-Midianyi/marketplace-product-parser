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
    public Path moveToProcessed(Path file) throws IOException{
        Path target = ensureDirectory(Path.of(properties.getProcessedDir())).resolve(file.getFileName());
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Moved file {} to {}", file.getFileName(), target);
        return target;
    }

    @Override
    public Path moveToFailed(Path file, String errorMessage) throws IOException{
        Path target = ensureDirectory(Path.of(properties.getFailedDir())).resolve(file.getFileName());
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        Path errorFile = target.resolveSibling(file.getFileName() + ERROR_FILE_SUFFIX);
        Files.writeString(errorFile, errorMessage);
        log.info("Moved file {} to failed directory {}", file.getFileName(), target);
        return target;
    }

    private Path ensureDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
        return dir;
    }
}
