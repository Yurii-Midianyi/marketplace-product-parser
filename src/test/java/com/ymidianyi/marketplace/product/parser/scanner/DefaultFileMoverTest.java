package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFileMoverTest {

    @TempDir
    Path tempDir;

    private Path inputDir;
    private Path processedDir;
    private Path failedDir;

    private DefaultFileMover mover;

    @BeforeEach
    void setUp() throws IOException {
        inputDir = tempDir.resolve("input");
        processedDir = tempDir.resolve("processed");
        failedDir = tempDir.resolve("failed");

        Files.createDirectories(inputDir);

        FileProcessingProperties properties = mock(FileProcessingProperties.class);
        when(properties.getProcessedDir()).thenReturn(processedDir.toString());
        when(properties.getFailedDir()).thenReturn(failedDir.toString());

        mover = new DefaultFileMover(properties);
    }

    @Test
    void moveToProcessed_shouldMoveFileAndRemoveOriginal() throws IOException {
        Path sourceFile = Files.writeString(inputDir.resolve("products.json"), "content");

        Path result = mover.moveToProcessed(sourceFile);

        assertThat(result).exists();
        assertThat(result).hasParentRaw(processedDir);
        assertThat(result.getFileName().toString()).isEqualTo("products.json");
        assertThat(sourceFile).doesNotExist();
    }

    @Test
    void moveToFailed_shouldMoveFileAndCreateErrorFile() throws IOException {
        Path sourceFile = Files.writeString(inputDir.resolve("bad_data.csv"), "invalid");
        String errorMessage = "Missing required field: name";

        Path result = mover.moveToFailed(sourceFile, errorMessage);

        assertThat(result).exists();
        assertThat(result.getParent()).isEqualTo(failedDir);
        assertThat(sourceFile).doesNotExist();

        Path errorFile = failedDir.resolve("bad_data.csv.error");
        assertThat(errorFile).exists();
        assertThat(Files.readString(errorFile)).isEqualTo(errorMessage);
    }

    @Test
    void moveToProcessed_shouldCreateTargetDirectoryIfMissing() throws IOException {
        assertThat(processedDir).doesNotExist();

        Path sourceFile = Files.writeString(inputDir.resolve("data.json"), "{}");

        Path result = mover.moveToProcessed(sourceFile);

        assertThat(processedDir).isDirectory();
        assertThat(result).exists();
    }
}
