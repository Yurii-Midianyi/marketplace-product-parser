package com.ymidianyi.marketplace.product.parser.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KafkaIngestionConfigTest {

    @Autowired
    private AppKafkaProperties properties;

    @Autowired
    private CommonErrorHandler kafkaCommonErrorHandler;

    @Test
    void shouldBindInboundKafkaProperties() {
        assertThat(properties.getIncomingTopic()).isEqualTo("product.exports.incoming");
        assertThat(properties.getConsumerGroupId()).isEqualTo("marketplace-product-parser-test");
        assertThat(properties.getIncomingDltTopic()).isEqualTo("product.exports.incoming.dlt");
        assertThat(properties.isIncomingAutoStartup()).isFalse();
        assertThat(properties.getIncomingConcurrency()).isEqualTo(1);
        assertThat(properties.getIncomingRetryBackoffMs()).isZero();
        assertThat(properties.getIncomingMaxRetries()).isZero();
    }

    @Test
    void shouldRegisterDefaultKafkaErrorHandler() {
        assertThat(kafkaCommonErrorHandler).isInstanceOf(DefaultErrorHandler.class);
    }
}
