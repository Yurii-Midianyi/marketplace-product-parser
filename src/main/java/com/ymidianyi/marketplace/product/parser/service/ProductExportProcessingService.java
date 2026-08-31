package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductExportProcessingService {

    private final FileParserFactory parserFactory;
    private final ProductExportValidator validator;
    private final ProductImportService importService;

    public ProductExportProcessingService(FileParserFactory parserFactory,
                                          ProductExportValidator validator,
                                          ProductImportService importService) {
        this.parserFactory = parserFactory;
        this.validator = validator;
        this.importService = importService;
    }

    public ProcessingResult process(IncomingProductExport incomingProductExport) {
        String sourceRef = sourceRefOf(incomingProductExport);
        try {
            FileParser parser = parserFactory.getParser(incomingProductExport.format());
            log.debug("Parsing '{}' with {}", sourceRef, parser.getClass().getSimpleName());

            ProductExportFileDto dto = parser.parse(incomingProductExport);
            ValidationResult validation = validator.validate(dto);
            if (!validation.valid()) {
                log.warn("Validation failed for '{}': {}", sourceRef, validation.errors());
                return ProcessingResult.validationError(sourceRef, validation.errors());
            }

            importService.importProducts(dto, sourceRef);
            return ProcessingResult.success(sourceRef);
        } catch (RuntimeException e) {
            log.error("Unexpected error processing '{}'", sourceRef, e);
            return ProcessingResult.parseError(sourceRef, e.toString());
        }
    }

    private static String sourceRefOf(IncomingProductExport incomingProductExport) {
        if (incomingProductExport.sourceRef() != null && !incomingProductExport.sourceRef().isBlank()) {
            return incomingProductExport.sourceRef();
        }
        return incomingProductExport.partnerId() + ":" + incomingProductExport.exportDate();
    }
}
