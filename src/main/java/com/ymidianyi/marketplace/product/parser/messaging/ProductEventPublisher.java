package com.ymidianyi.marketplace.product.parser.messaging;

import com.ymidianyi.marketplace.product.parser.config.KafkaProperties;
import com.ymidianyi.marketplace.product.parser.event.ProductUpsertedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class ProductEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaProperties kafkaProperties;


    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProperties = kafkaProperties;
    }

    public void productUpserted(ProductUpsertedEvent productUpsertedEvent) {
        String topic = kafkaProperties.getProductUpsertedTopic();
        try{
            String payload = objectMapper.writeValueAsString(productUpsertedEvent);
            kafkaTemplate.send(topic, productUpsertedEvent.messageKey(), payload)
                    .whenComplete((result, exception) -> {
                        if(exception == null){
                            log.debug("Published product upserted for {} to {}", productUpsertedEvent.messageKey(), topic);
                        }
                        else{
                            log.error("Delivery of product upserted for {} to {} failed", productUpsertedEvent.messageKey(), topic, exception);
                        }
                    });
        }
        catch (RuntimeException e){
            log.error("Could not publish product upserted for {} to {}", productUpsertedEvent.messageKey(), topic, e);
        }
    }
}
