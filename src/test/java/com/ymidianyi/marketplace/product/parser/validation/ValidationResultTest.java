package com.ymidianyi.marketplace.product.parser.validation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationResultTest {

    @Test
    void okHasNoErrorsAndIsValid() {
        ValidationResult result = ValidationResult.ok();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void invalidVarargsCarriesErrors() {
        ValidationResult result = ValidationResult.invalid("a", "b");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("a", "b");
    }

    @Test
    void invalidWithEmptyListCollapsesToOk() {
        ValidationResult result = ValidationResult.invalid(List.of());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void invalidNoArgsCollapsesToOk() {
        ValidationResult result = ValidationResult.invalid();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void mergeOfTwoOkIsOk() {
        ValidationResult result = ValidationResult.ok().merge(ValidationResult.ok());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void mergeOfOkAndInvalidIsInvalidWithErrors() {
        ValidationResult result = ValidationResult.ok()
                .merge(ValidationResult.invalid("e1"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("e1");
    }

    @Test
    void mergeConcatenatesErrorsAndPreservesOrder() {
        ValidationResult result = ValidationResult.invalid("a", "b")
                .merge(ValidationResult.invalid("c"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("a", "b", "c");
    }

    @Test
    void mergeWithOkReturnsOriginalInvalid() {
        ValidationResult invalid = ValidationResult.invalid("e1");

        assertThat(invalid.merge(ValidationResult.ok())).isSameAs(invalid);
        assertThat(ValidationResult.ok().merge(invalid)).isSameAs(invalid);
    }

    @Test
    void errorsListIsImmutable() {
        ValidationResult result = ValidationResult.invalid("a");

        assertThat(result.errors()).isUnmodifiable();
    }

    @Test
    void errorsListSnapshotIsIndependentOfCallerMutation() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        ValidationResult result = new ValidationResult(source);

        source.add("mutated");

        assertThat(result.errors()).containsExactly("a", "b");
    }

    @Test
    void canonicalConstructorRejectsNullErrors() {
        assertThatThrownBy(() -> new ValidationResult(null))
                .isInstanceOf(NullPointerException.class);
    }
}
