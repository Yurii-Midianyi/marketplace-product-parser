package com.ymidianyi.marketplace.product.parser.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConfigurationProperties(prefix = "file-processing")
public class FileProcessingProperties {

    @Getter
    @Setter
    private String inputDir;

    @Getter
    @Setter
    private String processedDir;

    @Getter
    @Setter
    private String failedDir;

    @Getter
    @Setter
    private String cron;

    @Getter
    @Setter
    private int threadPoolSize;

    @Getter
    @Setter
    private int maxExportAgeDays;

    @PostConstruct
    public void init(){
        try {
            Files.createDirectories(Path.of(inputDir));
            Files.createDirectories(Path.of(processedDir));
            Files.createDirectories(Path.of(failedDir));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        log.info(inputDir);
        log.info(processedDir);
        log.info(failedDir);
        log.info(cron);
        log.info(String.valueOf(threadPoolSize));
        log.info(String.valueOf(maxExportAgeDays));
    }
}
