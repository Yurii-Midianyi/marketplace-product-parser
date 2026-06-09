package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductImportService;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Component
public class SingleFileProcessor {

    private final FileParserFactory parserFactory;
    private final ProductExportValidator validator;
    private final ProductImportService importService;
    private final FileMover fileMover;

    public SingleFileProcessor(FileParserFactory parserFactory,
                                ProductExportValidator validator,
                                ProductImportService importService,
                                FileMover fileMover) {
        this.parserFactory = parserFactory;
        this.validator = validator;
        this.importService = importService;
        this.fileMover = fileMover;
    }

    /**
     * Runs the full parse → validate → import → move pipeline for a single file.
     * This method never throws: any failure is caught and returned as a ProcessingResult.
     */
    public ProcessingResult process(Path file) {
        String fileName = file.getFileName().toString();
        try {
            return doProcess(file, fileName);
        } catch (Exception e) {
            log.error("Unexpected error processing file '{}'", fileName, e);
            silentlyMoveToFailed(file, fileName, e.toString());
            return ProcessingResult.parseError(fileName, e.toString());
        }
    }

    private ProcessingResult doProcess(Path file, String fileName) throws IOException {
        String extension = extensionOf(fileName);

        FileParser parser = parserFactory.getParser(extension);
        log.debug("Parsing '{}' with {}", fileName, parser.getClass().getSimpleName());
        ProductExportFileDto dto = parser.parse(file);

        ValidationResult validation = validator.validate(dto);
        if (!validation.valid()) {
            log.warn("Validation failed for '{}': {}", fileName, validation.errors());
            fileMover.moveToFailed(file, String.join("\n", validation.errors()));
            return ProcessingResult.validationError(fileName, validation.errors());
        }

        importService.importProducts(dto, fileName);
        fileMover.moveToProcessed(file);
        log.info("Successfully processed '{}'", fileName);
        return ProcessingResult.success(fileName);
    }

    private void silentlyMoveToFailed(Path file, String fileName, String detail) {
        try {
            fileMover.moveToFailed(file, detail);
        } catch (IOException ex) {
            log.error("Could not move '{}' to failed directory", fileName, ex);
        }
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
