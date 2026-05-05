package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportDateRangeValidatorTest {

    private static final Instant NOW = Instant.parse("2026-03-23T12:00:00Z");

    private ExportDateRangeValidator validator;

    @BeforeEach
    void setUp() {
        FileProcessingProperties properties = new FileProcessingProperties();
        properties.setMaxExportAgeDays(30);
        validator = new ExportDateRangeValidator(Clock.fixed(NOW, ZoneOffset.UTC), properties);
    }

    @Test
    void shouldAcceptDateWithinRange() {
        ProductExportFileDto dto = dtoWithExportDate(NOW.minus(Duration.ofDays(5)));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldAcceptDateAtNow() {
        ProductExportFileDto dto = dtoWithExportDate(NOW);

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldRejectDateInFuture() {
        ProductExportFileDto dto = dtoWithExportDate(NOW.plus(Duration.ofMinutes(1)));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .containsExactly("exportDate: must not be in the future");
    }

    @Test
    void shouldRejectDateOlderThanMaxAge() {
        ProductExportFileDto dto = dtoWithExportDate(NOW.minus(Duration.ofDays(31)));

        ValidationResult result = validator.validate(dto);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .containsExactly("exportDate: must not be older than 30 days");
    }

    @Test
    void shouldStaySilentWhenDtoOrDateIsNull() {
        assertThat(validator.validate(null).valid()).isTrue();
        assertThat(validator.validate(new ProductExportFileDto("PARTNER-A", null, List.of())).valid())
                .isTrue();
    }

    private static ProductExportFileDto dtoWithExportDate(Instant exportDate) {
        return new ProductExportFileDto("PARTNER-A", exportDate, List.of());
    }
}
