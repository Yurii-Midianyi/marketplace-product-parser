package com.ymidianyi.marketplace.product.parser.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ymidianyi.marketplace.product.parser.config.AppKafkaProperties;
import com.ymidianyi.marketplace.product.parser.event.ProductUpsertedEvent;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Publishes product domain events to Kafka as JSON.
 */
@Slf4j
@Component
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AppKafkaProperties properties;

    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper,
                                 AppKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void productUpserted(ProductUpsertedEvent event) {
        String topic = properties.getProductUpsertedTopic();
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.messageKey(), payload)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.debug("Published product.upserted for {} to {}", event.messageKey(), topic);
                        } else {
                            log.error("Delivery of product.upserted for {} to {} failed",
                                    event.messageKey(), topic, exception);
                        }
                    });
        } catch (RuntimeException e) {
            log.error("Could not publish product.upserted for {} to {}", event.messageKey(), topic, e);
        }
    }
}
