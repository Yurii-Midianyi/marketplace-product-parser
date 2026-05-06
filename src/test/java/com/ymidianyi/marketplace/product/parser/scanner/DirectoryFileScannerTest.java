package com.ymidianyi.marketplace.product.parser.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DirectoryFileScannerTest {

    @TempDir
    Path sharedTempDir;

    DirectoryFileScanner scanner = new DirectoryFileScanner();

    @Test
    public void testScanWithProperFiles() throws IOException {
        // 1. Define path for directory
        Path successfulFolder = sharedTempDir.resolve("processed");

        // 2. Create the actual directory on disk
        Files.createDirectory(successfulFolder);

        // 3. Define and create files inside directory
        Path fileCsv = successfulFolder.resolve("summary.csv");
        Path fileJson = successfulFolder.resolve("summary.json");
        Path fileJson2 = successfulFolder.resolve("summary2.json");
        Path fileXml = successfulFolder.resolve("debug.xml");

        Files.createFile(fileCsv);
        Files.createFile(fileJson);
        Files.createFile(fileJson2);
        Files.createFile(fileXml);

        // 4. Verification
        assertThat(fileCsv).exists().isRegularFile();
        assertThat(fileJson).exists().isRegularFile();
        assertThat(fileJson2).exists().isRegularFile();
        assertThat(fileXml).exists().isRegularFile();

        List<Path> results = scanner.scan(successfulFolder);
        assertThat(successfulFolder).isDirectory();
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
        assertThat(results.contains(fileCsv)).isTrue();
        assertThat(results.contains(fileJson)).isTrue();
        assertThat(results.contains(fileJson2)).isTrue();
        assertThat(results.contains(fileXml)).isFalse();
    }

    @Test
    public void testScanWithoutFiles() throws IOException {
        // 1. Define path for directory
        Path folder = sharedTempDir.resolve("empty");

        // 2. Create the actual directory on disk
        Files.createDirectory(folder);

        List<Path> results = scanner.scan(folder);
        assertThat(folder).isDirectory();
        assertThat(results).isEmpty();
    }

}
