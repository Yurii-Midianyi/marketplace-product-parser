package com.ymidianyi.marketplace.product.parser.processor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessingResultTest {

    @Test
    void success_shouldHaveSuccessStatusAndNoErrors() {
        ProcessingResult result = ProcessingResult.success("products.json");

        assertThat(result.fileName()).isEqualTo("products.json");
        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void validationError_shouldHaveValidationErrorStatusAndErrors() {
        List<String> errors = List.of("name: must not be blank", "price: must be positive");

        ProcessingResult result = ProcessingResult.validationError("bad.csv", errors);

        assertThat(result.fileName()).isEqualTo("bad.csv");
        assertThat(result.status()).isEqualTo(ProcessingStatus.VALIDATION_ERROR);
        assertThat(result.errors()).containsExactlyElementsOf(errors);
    }

    @Test
    void parseError_shouldHaveParseErrorStatusAndSingleErrorMessage() {
        ProcessingResult result = ProcessingResult.parseError("corrupt.json", "Unexpected end of JSON");

        assertThat(result.fileName()).isEqualTo("corrupt.json");
        assertThat(result.status()).isEqualTo(ProcessingStatus.PARSE_ERROR);
        assertThat(result.errors()).containsExactly("Unexpected end of JSON");
    }

    @Test
    void errorsListIsImmutable() {
        ProcessingResult result = ProcessingResult.validationError("bad.csv", List.of("e1", "e2"));

        assertThat(result.errors()).isUnmodifiable();
    }

    @Test
    void errorsListIsDefensivelyCopied() {
        List<String> mutableErrors = new java.util.ArrayList<>(List.of("e1"));
        ProcessingResult result = ProcessingResult.validationError("bad.csv", mutableErrors);

        mutableErrors.add("e2");

        assertThat(result.errors()).containsExactly("e1");
    }

    @Test
    void canonicalConstructorRejectsNullErrors() {
        assertThatThrownBy(() -> new ProcessingResult("file.json", ProcessingStatus.SUCCESS, null))
                .isInstanceOf(NullPointerException.class);
    }
}
