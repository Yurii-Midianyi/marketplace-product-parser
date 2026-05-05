package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeProductExportValidatorTest {

    private final ProductExportFileDto productExportFileDto =
            new ProductExportFileDto("PARTNER-A", Instant.parse("2026-03-23T10:30:00Z"), List.of());

    @Test
    void shouldReturnOkWhenAllRulesReturnOk() {
        CompositeProductExportValidator composite = new CompositeProductExportValidator(
                List.of(stubReturning(ValidationResult.ok()),
                        stubReturning(ValidationResult.ok())));

        ValidationResult result = composite.validate(productExportFileDto);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldRunEveryRuleAndMergeAllErrors() {
        List<String> callOrder = new ArrayList<>();
        CompositeProductExportValidator composite = new CompositeProductExportValidator(List.of(
                recording("v1", ValidationResult.invalid("e1"), callOrder),
                recording("v2", ValidationResult.ok(), callOrder),
                recording("v3", ValidationResult.invalid("e2", "e3"), callOrder)));

        ValidationResult result = composite.validate(productExportFileDto);

        assertThat(callOrder).containsExactly("v1", "v2", "v3");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("e1", "e2", "e3");
    }

    @Test
    void shouldHandleEmptyRuleList() {
        CompositeProductExportValidator composite = new CompositeProductExportValidator(List.of());

        ValidationResult result = composite.validate(productExportFileDto);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    private static ProductExportValidator stubReturning(ValidationResult result) {
        return dto -> result;
    }

    private static ProductExportValidator recording(String name,
                                                    ValidationResult result,
                                                    List<String> callOrder) {
        return dto -> {
            callOrder.add(name);
            return result;
        };
    }
}
