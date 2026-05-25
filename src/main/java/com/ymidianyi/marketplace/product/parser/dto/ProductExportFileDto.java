package com.ymidianyi.marketplace.product.parser.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductExportFileDto(@NotBlank String partnerId, @NotNull Instant exportDate, @NotEmpty List<@Valid ProductDto> products) {
}
