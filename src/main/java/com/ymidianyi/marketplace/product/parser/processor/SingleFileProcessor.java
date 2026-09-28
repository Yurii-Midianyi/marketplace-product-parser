package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.parser.FileNameParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParser;
import com.ymidianyi.marketplace.product.parser.parser.FileParserFactory;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;
import com.ymidianyi.marketplace.product.parser.service.ProductImportService;
import com.ymidianyi.marketplace.product.parser.validation.ProductExportValidator;
import com.ymidianyi.marketplace.product.parser.validation.ValidationResult;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SingleFileProcessor {

    private final ProductExportProcessingService processingService;
    private final FileMover fileMover;
    private final FileNameParser fileNameParser;

    public SingleFileProcessor(ProductExportProcessingService processingService,
                               FileMover fileMover,
                               FileNameParser fileNameParser) {
        this.processingService = processingService;
        this.fileMover = fileMover;
        this.fileNameParser = fileNameParser;
    }

    public ProcessingResult process(Path file) {
        try {
            return doProcess(file, file.getFileName().toString());
        } catch (Exception e) {
            log.error("Unexpected error processing file '{}'", file.getFileName(), e);
            silentlyMoveToFailed(file, file.getFileName().toString(), e.toString());
            return ProcessingResult.parseError(file.getFileName().toString(), e.toString());
        }
    }
    private ProcessingResult doProcess(Path file, String fileName) throws IOException {
        ProcessingResult result = processingService.process(toIncomingProductExport(file, fileName));
        if(result.status() != ProcessingStatus.SUCCESS){
            return moveToFailedPreservingResult(file, fileName, result);
        }

        fileMover.moveToProcessed(file);
        log.info("Successfully processed '{}'", fileName);
        return result;
    }

    private ProcessingResult moveToFailedPreservingResult(Path file, String fileName, ProcessingResult result) {
        try {
            fileMover.moveToFailed(file, String.join("\n", result.errors()));
        } catch (IOException e) {
            log.error("Could not move '{}' to failed directory'", fileName);
        }
        return result;
    }

    private void silentlyMoveToFailed(Path file, String fileName, String detail) {
        try {
            fileMover.moveToFailed(file, detail);
        } catch (IOException ex) {
            log.error("Could not move '{}' to failed directory", fileName, ex);
        }
    }

    private IncomingProductExport toIncomingProductExport(Path file, String fileName) throws IOException {
        IngestionFormat format = IngestionFormat.fromValue(extensionOf(fileName));
        FileNameMetadata metadata = fileNameParser.parseFileName(file);
        String payload = Files.readString(file);
        return new IncomingProductExport(metadata.partnerId(),
                metadata.exportDate(),
                format,
                fileName,
                payload);

    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot+1).toLowerCase() : "";
    }
}
