package com.ymidianyi.marketplace.product.parser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IncomingProductExport(@NotBlank String partnerId,
                                    @NotNull LocalDate exportDate,
                                    @NotNull IngestionFormat format,
                                    String sourceRef,
                                    @NotBlank String payload) {
}
