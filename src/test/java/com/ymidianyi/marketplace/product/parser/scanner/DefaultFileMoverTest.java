package com.ymidianyi.marketplace.product.parser.scanner;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DefaultFileMoverTest {

    @TempDir
    Path sharedTempDir;

    DefaultFileMover mover;

    FileProcessingProperties properties;

    @BeforeEach
    public void setup() {
        properties = new FileProcessingProperties();
        Path tempProcessed = sharedTempDir.resolve("processed");
        Path tempInput = sharedTempDir.resolve("input");
        Path tempFailed = sharedTempDir.resolve("failed");

        properties.setProcessedDir(String.valueOf(tempProcessed));
        properties.setInputDir(String.valueOf(tempInput));
        properties.setFailedDir(String.valueOf(tempFailed));

        mover = new DefaultFileMover(properties);
    }

    @Test
    public void fileIsMoved() throws IOException {
        Path targetFolder = sharedTempDir.resolve(properties.getProcessedDir());
        Path sourceFolder = sharedTempDir.resolve(properties.getInputDir());

        Files.createDirectory(targetFolder);
        Files.createDirectory(sourceFolder);

        Path fileCsv = sourceFolder.resolve("summary.csv");
        Path fileJson = sourceFolder.resolve("summary.json");

        Files.createFile(fileCsv);
        Files.createFile(fileJson);

        assertThat(fileCsv).exists().isRegularFile();
        assertThat(fileJson).exists().isRegularFile();

        mover.moveToProcessed(fileCsv);
        mover.moveToProcessed(fileJson);

        assertThat(targetFolder).isNotEmptyDirectory();
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.csv"));
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.json"));
        assertThat(sourceFolder).isEmptyDirectory();
    }

    @Test
    public void fileToFailed() throws IOException {
        Path targetFolder = sharedTempDir.resolve(properties.getFailedDir());
        Path sourceFolder = sharedTempDir.resolve(properties.getInputDir());

        Files.createDirectory(targetFolder);
        Files.createDirectory(sourceFolder);

        Path fileCsv = sourceFolder.resolve("summary.csv");
        Path fileJson = sourceFolder.resolve("summary.json");

        Files.createFile(fileCsv);
        Files.createFile(fileJson);

        assertThat(fileCsv).exists().isRegularFile();
        assertThat(fileJson).exists().isRegularFile();

        mover.moveToFailed(fileCsv, "This csv file is outdated");
        mover.moveToFailed(fileJson, "language is not supported");

        assertThat(targetFolder).isNotEmptyDirectory();
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.csv"));
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.csv.error"));
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.json"));
        assertThat(targetFolder).isDirectoryContaining(path ->
                path.getFileName().toString().equalsIgnoreCase("summary.json.error"));

        Path csvError = targetFolder.resolve("summary.csv.error");
        assertThat(csvError).hasContent("This csv file is outdated");
        Path jsonError = targetFolder.resolve("summary.json.error");
        assertThat(jsonError).hasContent("language is not supported");
        assertThat(sourceFolder).isEmptyDirectory();
    }

}
