package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

public interface ProductExportValidator {

    ValidationResult validate(ProductExportFileDto productExportFileDto);

}
