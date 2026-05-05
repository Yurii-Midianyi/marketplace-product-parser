package com.ymidianyi.marketplace.product.parser.validation;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
class SkuUniquenessWithinFileValidatorTest {
    private final SkuUniquenessWithinFileValidator validator = new SkuUniquenessWithinFileValidator();
    @Test
    void shouldAcceptUniqueSkus() {
        ProductExportFileDto dto = file(product("A1"), product("A2"), product("A3"));
        ValidationResult result = validator.validate(dto);
        assertThat(result.valid()).isTrue();
    }
    @Test
    void shouldReportDuplicateSkuWithBothIndexes() {
        ProductExportFileDto dto = file(product("A1"), product("A2"), product("A1"));
        ValidationResult result = validator.validate(dto);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .containsExactly("products[2].sku: duplicate of products[0].sku (\"A1\")");
    }
    @Test
    void shouldReportEachDuplicateOccurrenceSeparately() {
        ProductExportFileDto dto = file(
                product("A1"), product("A1"), product("A1"), product("B"));
        ValidationResult result = validator.validate(dto);
        assertThat(result.errors())
                .containsExactly(
                        "products[1].sku: duplicate of products[0].sku (\"A1\")",
                        "products[2].sku: duplicate of products[0].sku (\"A1\")");
    }
    @Test
    void shouldStaySilentForBlankOrNullSkus() {
        ProductExportFileDto dto = file(product(null), product(""), product(" "));
        ValidationResult result = validator.validate(dto);
        assertThat(result.valid()).isTrue();
    }
    @Test
    void shouldStaySilentWhenDtoOrProductsAreNull() {
        assertThat(validator.validate(null).valid()).isTrue();
        assertThat(validator.validate(new ProductExportFileDto("p", Instant.now(), null)).valid())
                .isTrue();
    }
    private static ProductExportFileDto file(ProductDto... products) {
        return new ProductExportFileDto("PARTNER-A", Instant.parse("2026-03-23T10:30:00Z"), List.of(products));
    }
    private static ProductDto product(String sku) {
        return new ProductDto(
                "Apple Fruit",
                sku,
                new BigDecimal("100"),
                null, null, null,
                ProductState.ACTIVE,
                null,
                List.of(),
                null);
    }
}
