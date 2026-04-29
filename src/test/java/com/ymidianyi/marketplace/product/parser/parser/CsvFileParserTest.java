package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.CsvParsingException;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.core.JacksonException;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CsvFileParserTest {

    @Autowired
    private CsvFileParser parser;

    private Path getResourcePath(String subdir) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/csv/" + subdir + "/products_PARTNER-A_2026-03-23.csv")
        ).toURI());
    }

    private Path getResourcePath(String subdir, String partnerId, String date) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/csv/" + subdir + "/products_" + partnerId + "_" + date + ".csv")
        ).toURI());
    }

    @Test
    void shouldSupportCsvExtension() {
        assertThat(parser.supports("csv")).isTrue();
        assertThat(parser.supports("CSV")).isTrue();
        assertThat(parser.supports("json")).isFalse();
    }

    @Test
    void shouldExtractPartnerIdAndExportDateFromFileName() throws URISyntaxException {
        Path file = getResourcePath("minimal");

        ProductExportFileDto result = parser.parse(file);

        assertThat(result.partnerId()).isEqualTo("PARTNER-A");

        Instant expectedDate = LocalDate.of(2026, 3, 23).atStartOfDay(ZoneOffset.UTC).toInstant();
        assertThat(result.exportDate()).isEqualTo(expectedDate);
    }

    @Test
    void shouldParseAllFieldsCorrectly() throws URISyntaxException {
        Path file = getResourcePath("all-fields");

        ProductExportFileDto result = parser.parse(file);
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
    void shouldHandleEmptyOptionalFields() throws URISyntaxException {
        Path file = getResourcePath("empty-optional", "PARTNER-B", "2026-03-20");

        ProductExportFileDto result = parser.parse(file);
        ProductDto product = result.products().getFirst();

        assertThat(product.specialPrice()).isNull();
        assertThat(product.specialFrom()).isNull();
        assertThat(product.specialTo()).isNull();
        assertThat(product.brand()).isEqualTo("Angel Of M");
        assertThat(product.categories()).isEmpty();
    }

    @Test
    void shouldWrapSingleCategoryIntoList() throws URISyntaxException {
        Path file = getResourcePath("single-category");

        ProductExportFileDto result = parser.parse(file);
        ProductDto product = result.products().getFirst();

        assertThat(product.categories()).containsExactly("Drinks");
    }

    @Test
    void shouldParseMultipleRows() throws URISyntaxException {
        Path file = getResourcePath("multiple-rows");

        ProductExportFileDto result = parser.parse(file);

        assertThat(result.products()).hasSize(3);
        assertThat(result.products().get(0).name()).isEqualTo("Banana Fruit");
        assertThat(result.products().get(1).name()).isEqualTo("Strawberry F");
        assertThat(result.products().get(2).name()).isEqualTo("Watermelon");
    }

    @Test
    void shouldThrowCsvParsingExceptionOnMalformedContent() throws URISyntaxException {
        Path file = getResourcePath("malformed");

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(CsvParsingException.class)
                .hasMessageContaining("products_PARTNER-A_2026-03-23.csv")
                .hasCauseInstanceOf(JacksonException.class);
    }
}
