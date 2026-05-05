package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

/**
 * Validation rule applied to a parsed export file.
 */
public interface ProductExportValidator {

    ValidationResult validate(ProductExportFileDto dto);
}
