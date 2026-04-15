package com.ymidianyi.marketplace.product.parser.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JacksonConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectMapper primaryMapper;

    @Autowired
    @Qualifier("csvObjectMapper")
    private ObjectMapper csvObjectMapper;

    @Test
    void shouldHaveAutoConfiguredJsonMapper() {
        assertThat(primaryMapper).isInstanceOf(JsonMapper.class);
    }

    @Test
    void shouldResolveCsvMapperByQualifier() {
        ObjectMapper csv = context.getBean("csvObjectMapper", ObjectMapper.class);
        assertThat(csv).isSameAs(csvObjectMapper);
    }

    @Test
    void csvObjectMapperShouldBeCsvMapper() {
        assertThat(csvObjectMapper).isInstanceOf(CsvMapper.class);
    }

    @Test
    void jsonMapperAndCsvMapperShouldBeDistinct() {
        assertThat(primaryMapper).isNotSameAs(csvObjectMapper);
    }

    @Test
    void jsonMapperShouldHandleInstant() {
        Instant now = Instant.parse("2026-03-23T10:30:00Z");
        String json = primaryMapper.writeValueAsString(now);
        Instant deserialized = primaryMapper.readValue(json, Instant.class);
        assertThat(deserialized).isEqualTo(now);
    }

    @Test
    void jsonMapperShouldHandleLocalDate() {
        LocalDate date = LocalDate.of(2026, 3, 1);
        String json = primaryMapper.writeValueAsString(date);
        LocalDate deserialized = primaryMapper.readValue(json, LocalDate.class);
        assertThat(deserialized).isEqualTo(date);
    }
}
