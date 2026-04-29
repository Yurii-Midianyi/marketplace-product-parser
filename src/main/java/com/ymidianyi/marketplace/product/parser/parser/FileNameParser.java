package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileNameParser {

    private static final Pattern FILE_NAME_PATTERN =
            Pattern.compile("products_(.+)_(\\d{4}-\\d{2}-\\d{2})\\.\\w+");

    public FileNameMetadata parse(String fileName) {
        Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "File name does not match expected convention 'products_{partnerId}_{yyyy-MM-dd}.ext': " + fileName);
        }

        String partnerId = matcher.group(1);
        String rawDate = matcher.group(2);
        try {
            LocalDate exportDate = LocalDate.parse(rawDate);
            return new FileNameMetadata(partnerId, exportDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "File name contains invalid date '" + rawDate + "': " + fileName, e);
        }
    }
}
