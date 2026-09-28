package com.ymidianyi.marketplace.product.parser.messaging;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingStatus;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaProductExportListener {

    private final ProductExportProcessingService processingService;

    public KafkaProductExportListener(ProductExportProcessingService processingService){
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "${app.kafka.incoming-topic}",
            groupId = "${app.kafka.consumer-group-id}",
            autoStartup = "${app.kafka.incoming-auto-startup}",
            concurrency = "${app.kafka.incoming-concurrency}"
    )
    public void consume(String payload,
                        @Header(name = "format", required = false) String format,
                        @Header(name = "partnerId", required = false) String partnerId,
                        @Header(name = "exportDate", required = false) String exportDate,
                        @Header(name = "sourceRef", required = false) String sourceRef,
                        @Header(name = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic){
        IncomingProductExport incomingProductExport = new IncomingProductExport(
                requireHeader("partnerId", partnerId),
                parseExportDate(exportDate),
                IngestionFormat.fromValue(requireHeader("format", format)),
                sourceRef(sourceRef, topic, partnerId, exportDate),
                payload
        );

        ProcessingResult result = processingService.process(incomingProductExport);
        if(result.status() != ProcessingStatus.SUCCESS){
            throw new IllegalStateException(String.format("Kafka import failed %s for %s : %s",
                    result.status(), result.fileName(), String.join("; ", result.errors())));
        }
        log.info("Kafka product export '{}' processed successfully", result.fileName());
    }

    private static String requireHeader(String name, String value){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(String.format("Missing required Kafka header %s", name));
        }
        return value;
    }

    private static LocalDate parseExportDate(String exportDate){
        String value = requireHeader("exportDate", exportDate);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Invalid date format: %s", value), e);
        }
    }

    private static String sourceRef(String sourceRef, String topic, String partnerId, String exportDate){
        if(sourceRef!= null && !sourceRef.isBlank()){
            return sourceRef;
        }
        String topicPart = topic != null && topic.isBlank() ? topic : "kafka";
        String partnerPart = partnerId != null && partnerId.isBlank() ? partnerId : "unknown-partner";
        String exportDatePart = exportDate != null && exportDate.isBlank() ? exportDate : "unknown-date";
        return String.format("%s : %s : %s", topicPart, partnerPart, exportDatePart);
    }
}
