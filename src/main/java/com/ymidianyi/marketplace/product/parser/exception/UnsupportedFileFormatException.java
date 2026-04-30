package com.ymidianyi.marketplace.product.parser.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnsupportedFileFormatException extends RuntimeException{

    public UnsupportedFileFormatException(String message){
        super(message);
    }

}
