package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.exception.CsvParsingException;
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
import java.time.ZoneOffset;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CsvFileParserTest {

    @Autowired
    private CsvFileParser parser;

    private Path getResourcePath(String subdir, String partnerId, String date) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/csv/" + subdir + "/products_" + partnerId + "_" + date + ".csv")
        ).toURI());
    }

    @Test
    void shouldSupportCsvFormat() {
        assertThat(parser.supports(IngestionFormat.CSV)).isTrue();
        assertThat(parser.supports(IngestionFormat.JSON)).isFalse();
    }

    @Test
    void shouldUseProvidedPartnerIdAndExportDate() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("minimal", "PARTNER-A", LocalDate.of(2026, 3, 23));

        ProductExportFileDto result = parser.parse(incomingProductExport);

        assertThat(result.partnerId()).isEqualTo("PARTNER-A");

        Instant expectedDate = LocalDate.of(2026, 3, 23).atStartOfDay(ZoneOffset.UTC).toInstant();
        assertThat(result.exportDate()).isEqualTo(expectedDate);
    }

    @Test
    void shouldParseAllFieldsCorrectly() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("all-fields", "PARTNER-A", LocalDate.of(2026, 3, 23));

        ProductExportFileDto result = parser.parse(incomingProductExport);
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
    void shouldHandleEmptyOptionalFields() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("empty-optional", "PARTNER-B", LocalDate.of(2026, 3, 20));

        ProductExportFileDto result = parser.parse(incomingProductExport);
        ProductDto product = result.products().getFirst();

        assertThat(product.specialPrice()).isNull();
        assertThat(product.specialFrom()).isNull();
        assertThat(product.specialTo()).isNull();
        assertThat(product.brand()).isEqualTo("Angel Of M");
        assertThat(product.categories()).isEmpty();
    }

    @Test
    void shouldWrapSingleCategoryIntoList() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("single-category", "PARTNER-A", LocalDate.of(2026, 3, 23));

        ProductExportFileDto result = parser.parse(incomingProductExport);
        ProductDto product = result.products().getFirst();

        assertThat(product.categories()).containsExactly("Drinks");
    }

    @Test
    void shouldParseMultipleRows() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("multiple-rows", "PARTNER-A", LocalDate.of(2026, 3, 23));

        ProductExportFileDto result = parser.parse(incomingProductExport);

        assertThat(result.products()).hasSize(3);
        assertThat(result.products().get(0).name()).isEqualTo("Banana Fruit");
        assertThat(result.products().get(1).name()).isEqualTo("Strawberry F");
        assertThat(result.products().get(2).name()).isEqualTo("Watermelon");
    }

    @Test
    void shouldThrowCsvParsingExceptionOnMalformedContent() throws Exception {
        IncomingProductExport incomingProductExport = incomingExport("malformed", "PARTNER-A", LocalDate.of(2026, 3, 23));

        assertThatThrownBy(() -> parser.parse(incomingProductExport))
                .isInstanceOf(CsvParsingException.class)
                .hasMessageContaining("products_PARTNER-A_2026-03-23.csv")
                .hasCauseInstanceOf(JacksonException.class);
    }

    private IncomingProductExport incomingExport(String subdir, String partnerId, LocalDate exportDate) throws Exception {
        Path file = getResourcePath(subdir, partnerId, exportDate.toString());
        return new IncomingProductExport(
                partnerId,
                exportDate,
                IngestionFormat.CSV,
                file.getFileName().toString(),
                Files.readString(file)
        );
    }
}
