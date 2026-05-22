package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.as;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

public class ExportDateRangeValidatorTest {

    Instant fixedInstant = Instant.parse("2000-05-20T10:15:30.00Z");
    Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
    FileProcessingProperties properties;
    ExportDateRangeValidator validator;

    @BeforeEach
    public void setup() {
        properties = new FileProcessingProperties();
        properties.setMaxExportAgeDays(30);
        validator = new ExportDateRangeValidator(properties, fixedClock);
    }

    @Test
    public void testValidateExportDateInFuture() throws  Exception {
        List<ProductDto> products = List.of(TestUtilities.createCheeseProduct());
        ProductExportFileDto NoPartnerDto = new ProductExportFileDto("PartnerA", Instant.now(), products);
        assertThat(validator.validate(NoPartnerDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("Export date can not be after the current date");
    }

    @Test
    public void testValidateExportDateOlderThan30Days() throws  Exception {
        List<ProductDto> products = List.of(TestUtilities.createCheeseProduct());
        ProductExportFileDto NoPartnerDto =
                new ProductExportFileDto("PartnerA", fixedClock.instant().minus(properties.getMaxExportAgeDays()+1, ChronoUnit.DAYS), products);
        assertThat(validator.validate(NoPartnerDto))
                .isNotNull()
                .extracting(ValidationResult::errors, as(LIST))
                .isNotEmpty()
                .contains("The file is older then allowed max export age");
    }
}
