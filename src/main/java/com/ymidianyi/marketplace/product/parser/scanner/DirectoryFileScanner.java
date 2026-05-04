package com.ymidianyi.marketplace.product.parser.scanner;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DirectoryFileScanner implements FileScanner {

    @Override
    public List<Path> scan(Path directory) {
        List<Path> result = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{json,csv}")){
            for(Path path : stream){
                result.add(path);
            }
            return result.stream().sorted().toList();
        } catch (IOException e) {
            log.error("Scanning failed {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
