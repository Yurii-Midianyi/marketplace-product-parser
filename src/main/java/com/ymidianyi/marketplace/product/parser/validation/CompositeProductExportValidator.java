package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompositeProductExportValidator implements ProductExportValidator {

    private final List<ProductExportValidator> rules;

    public CompositeProductExportValidator(List<ProductExportValidator> rules) {
        this.rules = List.copyOf(rules);
        log.info("Registered product-export validation rules: {}", this.rules.stream()
                .map(r -> r.getClass().getSimpleName())
                .toList());
    }

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        ValidationResult result = ValidationResult.ok();
        for (ProductExportValidator rule : rules) {
            result = result.merge(rule.validate(dto));
        }
        return result;
    }
}
