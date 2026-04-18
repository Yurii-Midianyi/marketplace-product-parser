package com.ymidianyi.marketplace.product.parser.exception;

public class UnsupportedFileFormatException extends RuntimeException {

    public UnsupportedFileFormatException(String extension) {
        super("No parser available for file extension: " + extension);
    }
}
