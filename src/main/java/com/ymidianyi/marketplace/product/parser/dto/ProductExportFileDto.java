package com.ymidianyi.marketplace.product.parser.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ProductExportFileDto(@NotBlank String partnerId,
                                   @NotNull Instant exportDate,
                                   @NotEmpty List<@Valid ProductDto> products) {
}
