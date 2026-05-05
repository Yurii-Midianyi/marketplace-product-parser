package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class ExportDateRangeValidator implements ProductExportValidator {

    private final Clock clock;
    private final int maxExportAgeDays;

    public ExportDateRangeValidator(Clock clock, FileProcessingProperties properties) {
        this.clock = clock;
        this.maxExportAgeDays = properties.getMaxExportAgeDays();
    }

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        if (dto == null || dto.exportDate() == null) {
            return ValidationResult.ok();
        }
        Instant now = Instant.now(clock);
        Instant exportDate = dto.exportDate();

        if (exportDate.isAfter(now)) {
            return ValidationResult.invalid("exportDate: must not be in the future");
        }
        Instant earliestAllowed = now.minus(Duration.ofDays(maxExportAgeDays));
        if (exportDate.isBefore(earliestAllowed)) {
            return ValidationResult.invalid(
                    "exportDate: must not be older than " + maxExportAgeDays + " days");
        }
        return ValidationResult.ok();
    }
}
