package com.ymidianyi.marketplace.product.parser.dto;

import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;

import java.util.Locale;

public enum IngestionFormat {
    JSON,
    CSV;

    public static IngestionFormat fromValue(String value) {
        try {
            return IngestionFormat.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedFileFormatException(value);
        }
    }
}
