package com.ymidianyi.marketplace.product.parser.processor;

import java.util.List;

public record ProcessingResult (
        String fileName,
        ProcessingStatus status,
        List<String> errors
){

    public ProcessingResult{
        errors = List.copyOf(errors);
    }

    public static ProcessingResult success(String fileName){
        return new ProcessingResult(fileName, ProcessingStatus.SUCCESS, List.of());
    }

    public static ProcessingResult validationError(String fileName, List<String> errors){
        return new ProcessingResult(fileName, ProcessingStatus.VALIDATION_ERROR, errors);
    }

    public static ProcessingResult parseError(String fileName, String message){
        return new ProcessingResult(fileName, ProcessingStatus.PARSE_ERROR, List.of(message));
    }
}
