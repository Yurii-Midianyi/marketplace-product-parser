package com.ymidianyi.marketplace.product.parser.config;

import com.ymidianyi.marketplace.product.parser.validation.BeanConstraintValidator;
import com.ymidianyi.marketplace.product.parser.validation.CompositeProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ExportDateRangeValidator;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.SkuUniquenessWithinFileValidator;
import com.ymidianyi.marketplace.product.parser.validation.SpecialPricingValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class ValidatorConfig {

    /**
     * Explicit rule order:
     * 1. BeanConstraintValidator - field-level constraints (null/blank/positive checks)
     * 2. ExportDateRangeValidator - file is not too old and not from the future
     * 3. SkuUniquenessWithinFileValidator - no duplicate SKUs within the same file
     * 4. SpecialPricingValidator - special price < base price, dates required and ordered
     */
    @Bean
    @Primary
    public ProductExportValidator compositeValidator(
            BeanConstraintValidator beanConstraintValidator,
            ExportDateRangeValidator exportDateRangeValidator,
            SkuUniquenessWithinFileValidator skuUniquenessWithinFileValidator,
            SpecialPricingValidator specialPricingValidator) {

        return new CompositeProductExportValidator(List.of(
                beanConstraintValidator,
                exportDateRangeValidator,
                skuUniquenessWithinFileValidator,
                specialPricingValidator
        ));
    }
}

