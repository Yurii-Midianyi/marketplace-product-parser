package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialPricingValidatorTest {

    private final SpecialPricingValidator validator = new SpecialPricingValidator();

    @Test
    void shouldStaySilentWhenSpecialPriceAbsent() {
        ProductDto product = baseProduct().withSpecialPrice(null, null, null).build();

        ValidationResult result = validator.validate(file(product));

        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptValidSpecialPricing() {
        ProductDto product = baseProduct().withSpecialPrice(
                new BigDecimal("90"), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)).build();

        ValidationResult result = validator.validate(file(product));

        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldReportMissingSpecialFromAndSpecialTo() {
        ProductDto product = baseProduct().withSpecialPrice(new BigDecimal("90"), null, null).build();

        ValidationResult result = validator.validate(file(product));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .contains("products[0].specialFrom: required when specialPrice is set",
                        "products[0].specialTo: required when specialPrice is set");
    }

    @Test
    void shouldReportSpecialFromNotBeforeSpecialTo() {
        ProductDto product = baseProduct().withSpecialPrice(
                new BigDecimal("90"), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1)).build();

        ValidationResult result = validator.validate(file(product));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .contains("products[0].specialFrom: must be before specialTo");
    }

    @Test
    void shouldReportSpecialPriceNotLessThanPrice() {
        ProductDto product = baseProduct().withSpecialPrice(
                new BigDecimal("100"), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)).build();

        ValidationResult result = validator.validate(file(product));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .contains("products[0].specialPrice: must be less than price");
    }

    @Test
    void shouldUseCorrectIndexForMultipleProducts() {
        ProductDto good = baseProduct().withSpecialPrice(null, null, null).build();
        ProductDto bad = baseProduct().withSpecialPrice(new BigDecimal("90"), null, null).build();
        ProductExportFileDto dto = file(good, bad);

        ValidationResult result = validator.validate(dto);

        assertThat(result.errors())
                .allMatch(e -> e.startsWith("products[1]."));
    }

    @Test
    void shouldStaySilentWhenDtoOrProductsAreNull() {
        assertThat(validator.validate(null).valid()).isTrue();
        assertThat(validator.validate(new ProductExportFileDto("p", Instant.now(), null)).valid())
                .isTrue();
    }

    @Test
    void shouldSkipPriceComparisonWhenBaseProductPriceIsNull() {
        ProductDto product = new ProductDto(
                "Apple Fruit", "802999",
                null,                             // price is null
                new BigDecimal("90"),              // specialPrice is set
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1),
                ProductState.ACTIVE, null, List.of(), null);

        ValidationResult result = validator.validate(file(product));

        assertThat(result.errors())
                .noneMatch(e -> e.contains("specialPrice: must be less than price"));
    }

    private static ProductExportFileDto file(ProductDto... products) {
        return new ProductExportFileDto("PARTNER-A", Instant.parse("2026-03-23T10:30:00Z"), List.of(products));
    }

    private static TestProductBuilder baseProduct() {
        return new TestProductBuilder();
    }

    private static final class TestProductBuilder {
        private BigDecimal specialPrice;
        private LocalDate specialFrom;
        private LocalDate specialTo;

        TestProductBuilder withSpecialPrice(BigDecimal price, LocalDate from, LocalDate to) {
            this.specialPrice = price;
            this.specialFrom = from;
            this.specialTo = to;
            return this;
        }

        ProductDto build() {
            return new ProductDto(
                    "Apple Fruit",
                    "802999",
                    new BigDecimal("100"),
                    specialPrice,
                    specialFrom,
                    specialTo,
                    ProductState.ACTIVE,
                    "Shelf 3",
                    List.of(),
                    null);
        }
    }
}
