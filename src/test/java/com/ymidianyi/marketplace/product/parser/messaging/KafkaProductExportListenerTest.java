package com.ymidianyi.marketplace.product.parser.messaging;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.processor.ProcessingResult;
import com.ymidianyi.marketplace.product.parser.service.ProductExportProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaProductExportListenerTest {

    private ProductExportProcessingService processingService;
    private KafkaProductExportListener listener;

    @BeforeEach
    void setUp() {
        processingService = mock(ProductExportProcessingService.class);
        listener = new KafkaProductExportListener(processingService);
    }

    @Test
    void consume_validHeaders_delegatesNormalizedCommand() {
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.success("kafka-source-42"));

        listener.consume(
                "{\"products\":[]}",
                "json",
                "PARTNER-A",
                "2026-08-25",
                "kafka-source-42",
                "product.exports.incoming"
        );

        ArgumentCaptor<IncomingProductExport> captor = ArgumentCaptor.forClass(IncomingProductExport.class);
        verify(processingService).process(captor.capture());
        IncomingProductExport command = captor.getValue();

        assertThat(command.partnerId()).isEqualTo("PARTNER-A");
        assertThat(command.exportDate()).isEqualTo(LocalDate.of(2026, Month.AUGUST, 25));
        assertThat(command.format()).isEqualTo(IngestionFormat.JSON);
        assertThat(command.sourceRef()).isEqualTo("kafka-source-42");
        assertThat(command.payload()).isEqualTo("{\"products\":[]}");
    }

    @Test
    void consume_blankPartnerHeader_throwsWithoutDelegating() {
        assertThatThrownBy(() -> listener.consume(
                "{\"products\":[]}",
                "json",
                " ",
                "2026-08-25",
                null,
                "product.exports.incoming"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partnerId");

        verify(processingService, never()).process(any());
    }

    @Test
    void consume_missingFormatHeader_throwsWithoutDelegating() {
        assertThatThrownBy(() -> listener.consume(
                "{\"products\":[]}",
                null,
                "PARTNER-A",
                "2026-08-25",
                null,
                "product.exports.incoming"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format");

        verify(processingService, never()).process(any());
    }

    @Test
    void consume_invalidExportDateHeader_throwsWithoutDelegating() {
        assertThatThrownBy(() -> listener.consume(
                "{\"products\":[]}",
                "json",
                "PARTNER-A",
                "25-08-2026",
                null,
                "product.exports.incoming"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exportDate");

        verify(processingService, never()).process(any());
    }

    @Test
    void consume_missingSourceRef_buildsTopicBasedFallback() {
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.success("product.exports.incoming:PARTNER-A:2026-08-25"));

        listener.consume(
                "name,sku,price,state\nApple,SKU-1,10.0,ACTIVE\n",
                "csv",
                "PARTNER-A",
                "2026-08-25",
                null,
                "product.exports.incoming"
        );

        verify(processingService).process(new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, Month.AUGUST, 25),
                IngestionFormat.CSV,
                "product.exports.incoming:PARTNER-A:2026-08-25",
                "name,sku,price,state\nApple,SKU-1,10.0,ACTIVE\n"
        ));
    }

    @Test
    void consume_failedProcessing_throwsListenerFailure() {
        when(processingService.process(any(IncomingProductExport.class)))
                .thenReturn(ProcessingResult.validationError(
                        "product.exports.incoming:PARTNER-A:2026-08-25",
                        List.of("sku: must not be blank")
                ));

        assertThatThrownBy(() -> listener.consume(
                "{\"products\":[]}",
                "json",
                "PARTNER-A",
                "2026-08-25",
                null,
                "product.exports.incoming"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VALIDATION_ERROR")
                .hasMessageContaining("sku: must not be blank");
    }
}
