package com.ymidianyi.marketplace.product.parser.validation;

import com.ymidianyi.marketplace.product.parser.config.FileProcessingProperties;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class ExportDateRangeValidator implements ProductExportValidator{

    private final int maxExportAge;
    private final Clock clock;

    public ExportDateRangeValidator(FileProcessingProperties fileProcessingProperties, Clock clock) {
        this.maxExportAge = fileProcessingProperties.getMaxExportAgeDays();
        this.clock = clock;
    }

    @Override
    public ValidationResult validate(ProductExportFileDto dto) {
        /*
         Validation where dto==null writes an error is done in BeanConstraintValidator to omit a case where all
         the validators return the same message multiple times
        */
        if (dto == null || dto.exportDate() == null) {
            return ValidationResult.ok();
        }
        Instant now = clock.instant();
        if(dto.exportDate().isAfter(now)){
            return ValidationResult.invalid("Export date can not be after the current date");
        }

        if(now.minus(Duration.ofDays(maxExportAge)).isAfter(dto.exportDate())){
            return ValidationResult.invalid("The file is older then allowed max export age");
        }
       return ValidationResult.ok();
    }
}
