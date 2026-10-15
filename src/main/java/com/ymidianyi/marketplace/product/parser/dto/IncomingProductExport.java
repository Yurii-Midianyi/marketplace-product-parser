package com.ymidianyi.marketplace.product.parser.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IncomingProductExport(@NotBlank String partnerId,
                                    @NotNull LocalDate exportDate,
                                    @NotNull IngestionFormat format,
                                    String sourceRef,
                                    @NotBlank String payload) {
}
