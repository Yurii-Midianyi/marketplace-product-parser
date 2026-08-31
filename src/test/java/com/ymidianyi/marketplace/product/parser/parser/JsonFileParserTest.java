package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.JsonParsingException;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.core.JacksonException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JsonFileParserTest {

    @Autowired
    private JsonFileParser parser;

    private Path getResourcePath(String resourceName) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/json/" + resourceName)
        ).toURI());
    }

    @Test
    void shouldSupportJsonFormat() {
        assertThat(parser.supports(IngestionFormat.JSON)).isTrue();
        assertThat(parser.supports(IngestionFormat.CSV)).isFalse();
    }

    @Test
    void shouldParseValidJsonWithAllFields() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport(
                "valid_all_fields.json",
                "PARTNER-A",
                LocalDate.of(2026, 3, 23)
        );

        ProductExportFileDto result = parser.parse(incomingProductExport);

        assertThat(result.partnerId()).isEqualTo("PARTNER-A");
        assertThat(result.exportDate()).isEqualTo(Instant.parse("2026-03-23T00:00:00Z"));
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
    void shouldParseJsonWithOptionalFieldsNull() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport(
                "valid_minimal_fields.json",
                "PARTNER-OVERRIDE",
                LocalDate.of(2026, 3, 24)
        );

        ProductExportFileDto result = parser.parse(incomingProductExport);

        assertThat(result.partnerId()).isEqualTo("PARTNER-OVERRIDE");
        assertThat(result.exportDate()).isEqualTo(Instant.parse("2026-03-24T00:00:00Z"));
        ProductDto product = result.products().getFirst();
        assertThat(product.specialPrice()).isNull();
        assertThat(product.specialFrom()).isNull();
        assertThat(product.specialTo()).isNull();
        assertThat(product.brand()).isNull();
        assertThat(product.imageUrl()).isNull();
        assertThat(product.categories()).isEmpty();
    }

    @Test
    void shouldThrowJsonParsingExceptionOnMalformedContent() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport(
                "malformed.json",
                "PARTNER-A",
                LocalDate.of(2026, 3, 23)
        );

        assertThatThrownBy(() -> parser.parse(incomingProductExport))
                .isInstanceOf(JsonParsingException.class)
                .hasMessageContaining("malformed.json")
                .hasCauseInstanceOf(JacksonException.class);
    }

    private IncomingProductExport incomingExport(String resourceName, String partnerId, LocalDate exportDate) throws Exception {
        Path file = getResourcePath(resourceName);
        return new IncomingProductExport(
                partnerId,
                exportDate,
                IngestionFormat.JSON,
                file.getFileName().toString(),
                Files.readString(file)
        );
    }
}