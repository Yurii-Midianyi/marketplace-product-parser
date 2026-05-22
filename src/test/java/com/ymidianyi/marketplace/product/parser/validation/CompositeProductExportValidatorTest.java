package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.as;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CompositeProductExportValidatorTest {

    @Autowired
    CompositeProductExportValidator validator;

    @MockitoBean
    Clock mockClock;

    @Test
    public void testValidateWithValidProductExportFileDto() {
        ProductDto productDto = TestUtilities.createCheeseProduct();
        when(mockClock.instant()).thenReturn(Instant.parse("2026-05-22T20:07:51Z"));
        ProductExportFileDto dto = new ProductExportFileDto("TestPartner", Instant.parse("2026-05-20T20:07:51Z"), List.of(productDto));
        assertThat(validator.validate(dto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isEmpty();
    }

    @Test
    public void testValidateWithInvalidProductExportFileDto() {
        ProductDto productDto = TestUtilities.createNotValidProduct();
        Instant oldInstant = Instant.parse("2019-08-20T00:00:00Z");
        ProductExportFileDto dto = new ProductExportFileDto("TestPartner", oldInstant, List.of(productDto));
        when(mockClock.instant()).thenReturn(Instant.parse("2026-05-22T20:07:51Z"));
        assertThat(validator.validate(dto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .contains("products[0].name: must not be blank",
                          "products[0].price: must be greater than 0",
                          "products[0].specialFrom: required when specialPrice is set",
                          "products[0].specialTo: required when specialPrice is set",
                          "products[0].specialPrice: must be less than price",
                          "The file is older then allowed max export age");
    }
}
