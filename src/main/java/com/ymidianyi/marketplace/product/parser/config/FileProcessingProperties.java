package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

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
        System.out.println(inputDir);
        System.out.println(processedDir);
        System.out.println(failedDir);
        System.out.println(cron);
        System.out.println(threadPoolSize);
        System.out.println(maxExportAgeDays);
    }
}
