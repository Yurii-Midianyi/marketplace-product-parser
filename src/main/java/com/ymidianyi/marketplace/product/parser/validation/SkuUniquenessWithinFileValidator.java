package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates that no two products within the same export file have the same SKU.
 * Blank and null SKUs are ignored and reported by other validators.
 */
@Component
public class SkuUniquenessWithinFileValidator implements ProductExportValidator {

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        if (dto == null || dto.products() == null || dto.products().size() < 2) {
            return ValidationResult.ok();
        }
        List<String> errors = findDuplicateSkuErrors(dto.products());
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.invalid(errors);
    }

    private List<String> findDuplicateSkuErrors(List<ProductDto> products) {
        Map<String, Integer> firstSeenIndex = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            String sku = getSku(products.get(i));
            if (sku != null) {
                Integer existingIndex = firstSeenIndex.putIfAbsent(sku, i);
                if (existingIndex != null) {
                    errors.add(duplicateSkuError(i, existingIndex, sku));
                }
            }
        }
        return errors;
    }

    private static String getSku(ProductDto product) {
        if (product == null) return null;
        String sku = product.sku();
        return (sku == null || sku.isBlank()) ? null : sku;
    }

    private static String duplicateSkuError(int at, int firstAt, String sku) {
        return "products[%d].sku: duplicate of products[%d].sku (\"%s\")".formatted(at, firstAt, sku);
    }
}

