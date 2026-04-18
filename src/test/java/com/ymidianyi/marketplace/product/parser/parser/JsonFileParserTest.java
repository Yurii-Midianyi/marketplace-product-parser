package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFileParserTest {

    private JsonFileParser parser;

    @BeforeEach
    void setUp() {
        var objectMapper = JsonMapper.builder().findAndAddModules().build();
        parser = new JsonFileParser(objectMapper);
    }

    private Path getResourcePath(String resourceName) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/json/" + resourceName)
        ).toURI());
    }

    @Test
    void shouldSupportJsonExtension() {
        assertThat(parser.supports("json")).isTrue();
        assertThat(parser.supports("JSON")).isTrue();
        assertThat(parser.supports("csv")).isFalse();
    }

    @Test
    void shouldParseValidJsonWithAllFields() throws IOException, URISyntaxException {
        Path file = getResourcePath("valid_all_fields.json");

        ProductExportFileDto result = parser.parse(file);

        assertThat(result.partnerId()).isEqualTo("PARTNER-A");
        assertThat(result.exportDate()).isEqualTo(Instant.parse("2026-03-23T10:30:00Z"));
        assertThat(result.products()).hasSize(1);

        ProductDto product = result.products().getFirst();
        assertThat(product.name()).isEqualTo("Apple Fruit");
        assertThat(product.sku()).isEqualTo("802999");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("41238.0"));
        assertThat(product.specialPrice()).isEqualByComparingTo(new BigDecimal("35000.0"));
        assertThat(product.specialFrom()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(product.specialTo()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(product.state()).isEqualTo(ProductState.ACTIVE);
        assertThat(product.brand()).isEqualTo("Shelf 3");
        assertThat(product.categories()).containsExactly("Golden apple Bundles");
        assertThat(product.imageUrl()).isEqualTo("https://img.example.com/apple.jpg");
    }

    @Test
    void shouldParseJsonWithOptionalFieldsNull() throws IOException, URISyntaxException {
        Path file = getResourcePath("valid_minimal_fields.json");

        ProductExportFileDto result = parser.parse(file);

        ProductDto product = result.products().getFirst();
        assertThat(product.specialPrice()).isNull();
        assertThat(product.specialFrom()).isNull();
        assertThat(product.specialTo()).isNull();
        assertThat(product.brand()).isNull();
        assertThat(product.imageUrl()).isNull();
        assertThat(product.categories()).isEmpty();
    }
}
