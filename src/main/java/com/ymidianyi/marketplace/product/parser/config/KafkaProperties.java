package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    /** Topic that receives one event per product persisted by an import. */
    @NotBlank
    private String productUpsertedTopic;

    /** Topic that receives inbound product export payloads for import. */
    @NotBlank
    private String incomingTopic;

    /** Consumer group for the inbound product export listener. */
    @NotBlank
    private String consumerGroupId;

    /** Dead-letter topic for failed inbound product export messages. */
    @NotBlank
    private String incomingDltTopic;

    /** Whether the inbound Kafka listener should start automatically. */
    private boolean incomingAutoStartup = true;

    /** Number of concurrent Kafka consumer threads for inbound product exports. */
    @Min(1)
    private int incomingConcurrency = 1;

    /** Delay between Kafka redelivery attempts for retryable failures. */
    @Min(0)
    private long incomingRetryBackoffMs = 1000;

    /** Number of retries before a message is sent to the dead-letter topic. */
    @Min(0)
    private long incomingMaxRetries = 2;
}
