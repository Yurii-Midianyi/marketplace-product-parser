package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductImportService;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SingleFileProcessor {
    private final FileParserFactory fileParserFactory;
    private final ProductExportValidator validator;
    private final ProductImportService productImportService;
    private final FileMover fileMover;

    public SingleFileProcessor(FileParserFactory fileParserFactory,
                               ProductExportValidator validator,
                               ProductImportService productImportService,
                               FileMover fileMover) {
        this.fileParserFactory = fileParserFactory;
        this.validator = validator;
        this.productImportService = productImportService;
        this.fileMover = fileMover;
    }

    public ProcessingResult process(Path file) {
        try {
            return doProcess(file);
        } catch (Exception e) {
            log.error("Unexpected error processing file '{}'", file.getFileName(), e);
            silentlyMoveToFailed(file, file.getFileName().toString(), e.toString());
            return ProcessingResult.parseError(file.getFileName().toString(), e.toString());
        }
    }

    private ProcessingResult doProcess(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        FileParser fileParser = fileParserFactory.getParser(FileParser.extensionOf(fileName));
        log.debug("Parsing '{}' with {}", fileName, fileParser.getClass().getSimpleName());
        ProductExportFileDto exportDto = fileParser.parse(file);
        ValidationResult validationResult = validator.validate(exportDto);
        if(!validationResult.valid()){
            log.info("Validation errors were found while processing file {}", fileName);
            fileMover.moveToFailed(file, String.join("\n", validationResult.errors()));
            return ProcessingResult.validationError(fileName, validationResult.errors());
        }
        productImportService.importProducts(exportDto, fileName);
        fileMover.moveToProcessed(file);
        log.info("File {} was successfully processed", fileName);
        return ProcessingResult.success(fileName);
    }

    private void silentlyMoveToFailed(Path file, String fileName, String detail) {
        try {
            fileMover.moveToFailed(file, detail);
        } catch (IOException ex) {
            log.error("Could not move '{}' to failed directory", fileName, ex);
        }
    }
}
