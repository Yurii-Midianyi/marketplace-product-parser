package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefaultFileMover implements FileMover {

    private static final String ERROR_FILE_SUFFIX = ".error";

    private final Path processedDir;
    private final Path failedDir;

    public DefaultFileMover(FileProcessingProperties properties) {
        this.processedDir = Path.of(properties.getProcessedDir());
        this.failedDir = Path.of(properties.getFailedDir());
    }

    @Override
    public Path moveToProcessed(Path file) throws IOException {
        Path target = ensureDirectory(processedDir).resolve(file.getFileName());
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Moved '{}' to processed directory", file.getFileName());
        return target;
    }

    @Override
    public Path moveToFailed(Path file, String errorMessage) throws IOException {
        Path target = ensureDirectory(failedDir).resolve(file.getFileName());
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        writeErrorFile(target, errorMessage);
        log.warn("Moved '{}' to failed directory: {}", file.getFileName(), errorMessage);
        return target;
    }

    private Path ensureDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
        return dir;
    }

    private void writeErrorFile(Path failedFile, String errorMessage) throws IOException {
        Path errorFile = failedFile.resolveSibling(failedFile.getFileName() + ERROR_FILE_SUFFIX);
        Files.writeString(errorFile, errorMessage);
    }
}
