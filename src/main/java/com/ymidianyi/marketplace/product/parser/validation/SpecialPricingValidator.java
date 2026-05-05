package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpecialPricingValidator implements ProductExportValidator {

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        if (dto == null || dto.products() == null) {
            return ValidationResult.ok();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < dto.products().size(); i++) {
            errors.addAll(validateProduct(i, dto.products().get(i)));
        }
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.invalid(errors);
    }

    private List<String> validateProduct(int index, ProductDto productDto) {
        if (productDto == null || productDto.specialPrice() == null) {
            return List.of();
        }
        String productString = "products[" + index + "]";
        List<String> errors = new ArrayList<>();

        if (productDto.specialFrom() == null) {
            errors.add(productString + ".specialFrom: required when specialPrice is set");
        }
        if (productDto.specialTo() == null) {
            errors.add(productString + ".specialTo: required when specialPrice is set");
        }
        boolean datesPresent = productDto.specialFrom() != null && productDto.specialTo() != null;
        if (datesPresent && !productDto.specialFrom().isBefore(productDto.specialTo())) {
            errors.add(productString + ".specialFrom: must be before specialTo");
        }
        if (productDto.price() != null && productDto.specialPrice().compareTo(productDto.price()) >= 0) {
            errors.add(productString + ".specialPrice: must be less than price");
        }
        return errors;
    }
}
