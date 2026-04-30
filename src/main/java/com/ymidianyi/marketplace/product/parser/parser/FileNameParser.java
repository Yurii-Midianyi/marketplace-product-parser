package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileNameParser {

    private final String regex = "^products_([A-Za-z0-9-]+)_(\\d{4}-\\d{2}-\\d{2})\\.csv$";

    public FileNameMetadata parseFileName(Path file){
        String fileName = file.getFileName().toString();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            String partner = matcher.group(1);
            String date = matcher.group(2);
            LocalDate dateConverted = LocalDate.parse(date);
            return new FileNameMetadata(partner, dateConverted);
        }
        else throw new IllegalArgumentException("Invalid file name");
    }

}
