package com.ymidianyi.marketplace.product.parser.messaging;

import com.ymidianyi.marketplace.product.parser.config.AppKafkaProperties;
import com.ymidianyi.marketplace.product.parser.event.ProductUpsertedEvent;
import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductEventPublisherTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-03-23T10:00:00Z");

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);

    private ProductEventPublisher publisher;

    @BeforeEach
    void setUp() {
        AppKafkaProperties properties = new AppKafkaProperties();
        properties.setProductUpsertedTopic("product.upserted");
        publisher = new ProductEventPublisher(kafkaTemplate,
                JsonMapper.builder().findAndAddModules().build(), properties);
    }

    @Test
    void productUpserted_sendsJsonKeyedByPartnerAndSku() {
        SendResult<String, String> sendResult = new SendResult<>(
                new ProducerRecord<>("product.upserted", "PARTNER-A|SKU-1", "{}"), null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        publisher.productUpserted(ProductUpsertedEvent.from(product(), OCCURRED_AT));

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topic.capture(), key.capture(), payload.capture());

        assertThat(topic.getValue()).isEqualTo("product.upserted");
        assertThat(key.getValue()).isEqualTo("PARTNER-A|SKU-1");
        assertThat(payload.getValue())
                .contains("\"partnerId\":\"PARTNER-A\"")
                .contains("\"sku\":\"SKU-1\"")
                .contains("\"state\":\"ACTIVE\"")
                .contains("\"categories\":[\"Fruits\"]");
    }

    @Test
    void productUpserted_swallowsBrokerFailuresSoImportsKeepRunning() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("no broker"));

        assertThatCode(() -> publisher.productUpserted(ProductUpsertedEvent.from(product(), OCCURRED_AT)))
                .doesNotThrowAnyException();
    }

    @Test
    void productUpserted_swallowsAsyncDeliveryFailures() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("delivery timeout")));

        assertThatCode(() -> publisher.productUpserted(ProductUpsertedEvent.from(product(), OCCURRED_AT)))
                .doesNotThrowAnyException();
    }

    private Product product() {
        Product product = new Product();
        product.setPartnerId("PARTNER-A");
        product.setSku("SKU-1");
        product.setName("Apple");
        product.setPrice(new BigDecimal("10.00"));
        product.setState(ProductState.ACTIVE);
        product.setSourceFileName("products.json");
        product.setImportedAt(OCCURRED_AT);
        product.replaceCategories(Set.of(new Category("Fruits")));
        return product;
    }
}
