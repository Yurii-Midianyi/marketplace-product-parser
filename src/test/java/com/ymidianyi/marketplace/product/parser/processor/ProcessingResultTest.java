package com.ymidianyi.marketplace.product.parser.processor;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessingResultTest {
    @Test
    void testSuccess() {
        ProcessingResult processingResult = ProcessingResult.success("product.json");
        assertThat(processingResult.fileName()).isEqualTo("product.json");
        assertThat(processingResult.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(processingResult.errors()).isEmpty();
    }

    @Test
    void testValidationErrors() {
        List<String> errors = List.of("name: must not be blank", "price: must be positive");
        ProcessingResult processingResult = ProcessingResult.validationError("product.json", errors);
        assertThat(processingResult.fileName()).isEqualTo("product.json");
        assertThat(processingResult.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(processingResult.errors()).containsExactlyElementsOf(errors);
    }

    @Test
    void testParseError() {
        ProcessingResult processingResult = ProcessingResult.parseError("product.json", "file is not parsable");
        assertThat(processingResult.fileName()).isEqualTo("product.json");
        assertThat(processingResult.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(processingResult.errors()).contains("file is not parsable");
    }
}
