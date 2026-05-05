package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "file-processing")
public class FileProcessingProperties {
    @NotBlank
    private String inputDir;
    @NotBlank
    private String processedDir;
    @NotBlank
    private String failedDir;
    @NotBlank
    private String cron;
    @Min(1)
    private int threadPoolSize;
    @Min(1)
    private int maxExportAgeDays;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(inputDir));
            Files.createDirectories(Path.of(processedDir));
            Files.createDirectories(Path.of(failedDir));
        } catch (IOException e) {
            log.error(String.valueOf(e));
        }
        if (Files.notExists(Path.of(processedDir)) || Files.notExists(Path.of(failedDir)) || Files.notExists(Path.of(inputDir))) {
            log.error("Initial folders were not created during startup");
            throw new RuntimeException("Initial folders were not created during startup");
        }
        log.info("inputDir - {}, processedDir - {}, failedDir - {}, cron - {}, threadPoolSize- {}, maxExportAgeDays - {}",
                inputDir, processedDir, failedDir, cron, threadPoolSize, maxExportAgeDays);
    }
}
