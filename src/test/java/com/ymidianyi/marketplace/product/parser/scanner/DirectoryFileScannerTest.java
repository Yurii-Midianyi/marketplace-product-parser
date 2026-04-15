package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryFileScannerTest {

    private DirectoryFileScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new DirectoryFileScanner();
    }

    @Test
    void shouldReturnAllMatchingFiles() throws IOException {
        Files.createFile(tempDir.resolve("products_A.json"));
        Files.createFile(tempDir.resolve("products_B.json"));
        Files.createFile(tempDir.resolve("products_C.csv"));

        List<Path> result = scanner.scan(tempDir);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Path::getFileName)
                .extracting(Path::toString)
                .containsExactly("products_A.json", "products_B.json", "products_C.csv");
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() throws IOException {
        List<Path> result = scanner.scan(tempDir);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterOutUnsupportedExtensions() throws IOException {
        Files.createFile(tempDir.resolve("data.json"));
        Files.createFile(tempDir.resolve("data.csv"));
        Files.createFile(tempDir.resolve("data.xml"));
        Files.createFile(tempDir.resolve("readme.txt"));

        List<Path> result = scanner.scan(tempDir);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Path::getFileName)
                .extracting(Path::toString)
                .containsExactly("data.csv", "data.json");
    }

    @Test
    void shouldIgnoreSubdirectories() throws IOException {
        Files.createFile(tempDir.resolve("valid.json"));
        Files.createDirectory(tempDir.resolve("subdir.json"));

        List<Path> result = scanner.scan(tempDir);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFileName().toString()).isEqualTo("valid.json");
    }
}
