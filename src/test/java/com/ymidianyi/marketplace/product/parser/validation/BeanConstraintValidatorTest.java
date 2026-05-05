package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConstraintValidatorTest {

    private static ValidatorFactory factory;
    private static BeanConstraintValidator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        Validator delegate = factory.getValidator();
        validator = new BeanConstraintValidator(delegate);
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldReturnOkForFullyValidDto() {
        ProductExportFileDto dto = new ProductExportFileDto(
                "PARTNER-A",
                Instant.parse("2026-03-23T10:30:00Z"),
                List.of(validProduct()));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldReportBlankPartnerId() {
        ProductExportFileDto dto = new ProductExportFileDto(
                "  ",
                Instant.parse("2026-03-23T10:30:00Z"),
                List.of(validProduct()));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .anyMatch(e -> e.startsWith("partnerId:"));
    }

    @Test
    void shouldReportEmptyProducts() {
        ProductExportFileDto dto = new ProductExportFileDto(
                "PARTNER-A",
                Instant.parse("2026-03-23T10:30:00Z"),
                List.of());

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .anyMatch(e -> e.startsWith("products:"));
    }

    @Test
    void shouldCascadeIntoProductsAndReportNestedErrors() {
        ProductDto invalid = new ProductDto(
                " ",
                "",
                new BigDecimal("-1"),
                null, null, null,
                null,
                null, List.of(), null);
        ProductExportFileDto dto = new ProductExportFileDto(
                "PARTNER-A",
                Instant.parse("2026-03-23T10:30:00Z"),
                List.of(invalid));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .anyMatch(e -> e.startsWith("products[0].name:"))
                .anyMatch(e -> e.startsWith("products[0].sku:"))
                .anyMatch(e -> e.startsWith("products[0].price:"))
                .anyMatch(e -> e.startsWith("products[0].state:"));
    }

    @Test
    void shouldReturnInvalidForNullDto() {
        ValidationResult result = validator.validate(null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("dto: must not be null");
    }

    private static ProductDto validProduct() {
        return new ProductDto(
                "Apple Fruit",
                "802999",
                new BigDecimal("41238.0"),
                new BigDecimal("35000.0"),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 4, 1),
                ProductState.ACTIVE,
                "Shelf 3",
                List.of("Golden apple Bundles"),
                "https://img.example.com/apple.jpg");
    }
}
