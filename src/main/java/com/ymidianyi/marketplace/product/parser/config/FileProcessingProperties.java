package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "file-processing")
public class FileProcessingProperties {
    private String inputDir;
    private String processedDir;
    private String failedDir;
    private String cron;
    private int threadPoolSize;
    private int maxExportAgeDays;

    @PostConstruct
    public void init(){
        try {
            Files.createDirectories(Path.of(inputDir));
            Files.createDirectories(Path.of(processedDir));
            Files.createDirectories(Path.of(failedDir));
        }
        catch (IOException e){
            log.error(String.valueOf(e));
        }
        if(Files.notExists(Path.of(processedDir)) || Files.notExists(Path.of(failedDir)) || Files.notExists(Path.of(inputDir))){
            log.error("Initial folders were not created during startup");
            throw new RuntimeException("Initial folders were not created during startup");
        }
        log.info("inputDir - {}, processedDir - {}, failedDir - {}, cron - {}, threadPoolSize- {}, maxExportAgeDays - {}",
                inputDir, processedDir, failedDir, cron, threadPoolSize, maxExportAgeDays);
    }
}
