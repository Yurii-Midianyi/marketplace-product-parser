package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.as;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

public class SpecialPricingValidatorTest {

    SpecialPricingValidator specialPricingValidator = new SpecialPricingValidator();

    @Test
    public void testValidateSpecialPriceWithoutSpecialDates(){
        ProductDto productDto = TestUtilities.createNotValidProduct();
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("Partner1", Instant.now(), List.of(productDto));
        assertThat(specialPricingValidator.validate(productExportFileDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("products[0].specialFrom: required when specialPrice is set",
                          "products[0].specialTo: required when specialPrice is set",
                          "products[0].specialPrice: must be less than price");
    }

    @Test
    public void testValidatePriceLessThanSpecialPrice(){
        ProductDto productDto = new ProductDto(
                "Cheese",
                "5645",
                BigDecimal.valueOf(500), // price
                BigDecimal.valueOf(600), // specialPrice
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 5, 22),
                ProductState.ACTIVE,
                null,
                null,
                null
        );
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("Partner1", Instant.now(), List.of(productDto));
        assertThat(specialPricingValidator.validate(productExportFileDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("products[0].specialPrice: must be less than price");
    }

    /*
    Case when price is equal to null should be handled in other validator so
    in this case we do not return any errors
     */
    @Test
    public void testNullPriceWithSpecialPrice(){
        ProductDto productDto = new ProductDto(
                "Cheese",
                "5645",
                null, // price
                BigDecimal.valueOf(600), // specialPrice
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 5, 22),
                ProductState.ACTIVE,
                null,
                null,
                null
        );
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("Partner1", Instant.now(), List.of(productDto));
        assertThat(specialPricingValidator.validate(productExportFileDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isEmpty();
    }

    @Test
    public void testSpecialFromIsAfterSpecialTo(){
        ProductDto productDto = new ProductDto(
                "Cheese",
                "5645",
                BigDecimal.valueOf(700), // price
                BigDecimal.valueOf(600), // specialPrice
                LocalDate.of(2026, 6, 22),
                LocalDate.of(2026, 5, 22),
                ProductState.ACTIVE,
                null,
                null,
                null
        );
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("Partner1", Instant.now(), List.of(productDto));
        assertThat(specialPricingValidator.validate(productExportFileDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("products[0].specialFrom: must be before specialTo");
    }
}
