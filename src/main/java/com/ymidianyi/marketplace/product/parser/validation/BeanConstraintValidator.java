package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class BeanConstraintValidator implements ProductExportValidator {

    private final Validator validator;

    public BeanConstraintValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        if (dto == null) {
            return ValidationResult.invalid("dto: must not be null");
        }
        Set<ConstraintViolation<ProductExportFileDto>> violations = validator.validate(dto);
        if (violations.isEmpty()) {
            return ValidationResult.ok();
        }
        List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return ValidationResult.invalid(errors);
    }
}
