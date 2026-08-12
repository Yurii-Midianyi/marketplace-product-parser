package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    /** Topic that receives one event per product persisted by an import. */
    @NotBlank
    private String productUpsertedTopic;
}
