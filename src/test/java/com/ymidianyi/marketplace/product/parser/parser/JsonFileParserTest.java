package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileParserTest {

    ObjectMapper mapper = new JsonMapper();
    private JsonFileParser parser = new JsonFileParser(mapper);

    private Path getResourcePath(String resourceName) throws URISyntaxException {
        return Path.of(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/json/" + resourceName)
        ).toURI());
    }

    @Test
    public void testSupportedExtensions(){
        assertThat(parser.supports("json")).isTrue();
        assertThat(parser.supports("csv")).isFalse();
        assertThat(parser.supports("exe")).isFalse();
    }

    @Test
    public void testDataIsParsed() throws IOException, URISyntaxException {
        ProductExportFileDto productExportFileDto = parser.parse(getResourcePath("valid_all_fields.json"));
        assertThat(productExportFileDto).isNotNull();
        assertThat(productExportFileDto.partnerId()).isEqualTo("PARTNER-A");
        assertThat(productExportFileDto.exportDate()).isEqualTo(Instant.parse("2026-03-23T10:30:00Z"));
        assertThat(productExportFileDto.products()).hasSize(1);

        ProductDto productDto = productExportFileDto.products().getFirst();
        assertThat(productDto).isNotNull();
        assertThat(productDto.name()).isEqualTo("Apple Fruit");
        assertThat(productDto.sku()).isEqualTo("802999");
        assertThat(productDto.price()).isEqualByComparingTo(new BigDecimal("41238.0"));
        assertThat(productDto.specialPrice()).isEqualByComparingTo(new BigDecimal("35000.0"));
        assertThat(productDto.specialFrom()).isEqualTo("2026-03-01");
        assertThat(productDto.specialTo()).isEqualTo("2026-04-01");
        assertThat(productDto.state()).isEqualTo(ProductState.ACTIVE);
        assertThat(productDto.brand()).isEqualTo("Shelf 3");
        assertThat(productDto.categories()).contains("Golden apple Bundles");
        assertThat(productDto.imageUrl()).isEqualTo("https://img.example.com/apple.jpg");
    }

    @Test
    public void testSomeFieldsAreEmpty() throws IOException, URISyntaxException {
        ProductExportFileDto productExportFileDto = parser.parse(getResourcePath("valid_minimal_fields.json"));
        assertThat(productExportFileDto).isNotNull();
        assertThat(productExportFileDto.partnerId()).isEqualTo("PARTNER-B");
        assertThat(productExportFileDto.exportDate()).isEqualTo(Instant.parse("2026-03-20T08:00:00Z"));
        assertThat(productExportFileDto.products()).hasSize(1);

        ProductDto productDto = productExportFileDto.products().getFirst();
        assertThat(productDto).isNotNull();
        assertThat(productDto.name()).isEqualTo("Banana Fruit");
        assertThat(productDto.sku()).isEqualTo("000000001");
        assertThat(productDto.price()).isEqualByComparingTo(new BigDecimal("180.00000000000000")); //to test like a number, not a string
        assertThat(productDto.specialPrice()).isNull();
        assertThat(productDto.specialFrom()).isNull();
        assertThat(productDto.specialTo()).isNull();
        assertThat(productDto.state()).isEqualTo(ProductState.ACTIVE);
        assertThat(productDto.brand()).isNull();
        assertThat(productDto.categories()).isEmpty();
        assertThat(productDto.imageUrl()).isNull();
    }
}
