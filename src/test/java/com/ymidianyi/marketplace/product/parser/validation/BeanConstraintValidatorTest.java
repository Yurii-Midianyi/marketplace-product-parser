package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.as;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

public class BeanConstraintValidatorTest {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    BeanConstraintValidator beanConstraintValidator = new BeanConstraintValidator(validator);

    @Test
    public void testValidateEmptyPartnerId() {
        List<ProductDto> products = List.of(TestUtilities.createCheeseProduct());
        ProductExportFileDto NoPartnerDto = new ProductExportFileDto(null, Instant.now(), products);

        assertThat(beanConstraintValidator.validate(NoPartnerDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("partnerId: must not be blank");
    }

    @Test
    public void testValidateValidDto() {
        List<ProductDto> products = List.of(TestUtilities.createCheeseProduct());
        ProductExportFileDto NoPartnerDto = new ProductExportFileDto("PartnerA", Instant.now(), products);

        assertThat(beanConstraintValidator.validate(NoPartnerDto))
                .isNotNull()
                .isSameAs(ValidationResult.ok());
    }

    @Test
    public void testValidateProductNoName() {
        List<ProductDto> products = List.of(TestUtilities.createCheeseProduct(), TestUtilities.createNotValidProduct());
        ProductExportFileDto NoPartnerDto = new ProductExportFileDto("PartnerA", Instant.now(), products);

        assertThat(beanConstraintValidator.validate(NoPartnerDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("products[1].name: must not be blank", "products[1].price: must be greater than 0");
    }

}
