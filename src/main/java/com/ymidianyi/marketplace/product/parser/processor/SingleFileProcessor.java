package com.ymidianyi.marketplace.product.parser.processor;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.parser.FileNameParser;
import com.ymidianyi.marketplace.product.parser.scanner.FileMover;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    /**
     * File-specific adapter: reads file content, derives file metadata, delegates to the
     * shared processing core, then performs file movement based on the result.
     */
    public ProcessingResult process(Path file) {
        String fileName = file.getFileName().toString();
        MDC.put("file", fileName);
        try {
            return doProcess(file, fileName);
        } catch (Exception e) {
            log.error("Unexpected error processing file '{}'", fileName, e);
            silentlyMoveToFailed(file, fileName, e.toString());
            return ProcessingResult.parseError(fileName, e.toString());
        } finally {
            MDC.remove("file");
        }
    }

    private ProcessingResult doProcess(Path file, String fileName) throws IOException {
        ProcessingResult result = processingService.process(toIncomingProductExport(file, fileName));
        if (result.status() != ProcessingStatus.SUCCESS) {
            return moveToFailedPreservingResult(file, fileName, result);
        }

        fileMover.moveToProcessed(file);
        log.info("Successfully processed '{}'", fileName);
        return result;
    }

    private IncomingProductExport toIncomingProductExport(Path file, String fileName) throws IOException {
        IngestionFormat format = IngestionFormat.fromValue(extensionOf(fileName));
        FileNameMetadata metadata = fileNameParser.parse(fileName);
        String payload = Files.readString(file);
        return new IncomingProductExport(
                metadata.partnerId(),
                metadata.exportDate(),
                format,
                fileName,
                payload
        );
    }

    private ProcessingResult moveToFailedPreservingResult(Path file, String fileName, ProcessingResult result) {
        try {
            fileMover.moveToFailed(file, String.join("\n", result.errors()));
        } catch (IOException e) {
            log.error("Could not move '{}' to failed directory", fileName, e);
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

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
