package com.ymidianyi.marketplace.product.parser.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IncomingProductExportTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldAllowValidIncomingExportWithOptionalSourceRef() {
        IncomingProductExport command = new IncomingProductExport(
                "PARTNER-A",
                LocalDate.of(2026, 8, 25),
                IngestionFormat.JSON,
                null,
                "{\"products\":[]}");

        Set<ConstraintViolation<IncomingProductExport>> violations = validator.validate(command);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        IncomingProductExport command = new IncomingProductExport(
                " ",
                null,
                null,
                "kafka:product.exports.incoming:42",
                "");

        Set<ConstraintViolation<IncomingProductExport>> violations = validator.validate(command);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("partnerId", "exportDate", "format", "payload");
    }
}
