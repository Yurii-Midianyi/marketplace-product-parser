package com.ymidianyi.marketplace.product.parser.validation;

import java.util.List;
import java.util.stream.Stream;

public record ValidationResult(List<String> errors) {

    private static final ValidationResult OK = new ValidationResult(List.of());

    public ValidationResult {
        errors = List.copyOf(errors);
    }

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult invalid(String... errors) {
        return invalid(List.of(errors));
    }

    public static ValidationResult invalid(List<String> errors) {
        return errors.isEmpty() ? OK : new ValidationResult(errors);
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public ValidationResult merge(ValidationResult other) {
        if (errors.isEmpty()) {
            return other;
        }
        if (other.errors.isEmpty()) {
            return this;
        }
        return new ValidationResult(
                Stream.concat(errors.stream(), other.errors.stream()).toList());
    }
}
