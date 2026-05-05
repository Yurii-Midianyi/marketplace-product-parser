package com.ymidianyi.marketplace.product.parser.dto;

import com.ymidianyi.marketplace.product.parser.model.ProductState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductDto(@NotBlank String name,
                         @NotBlank String sku,
                         @NotNull @Positive BigDecimal price,
                         @Positive BigDecimal specialPrice,
                         LocalDate specialFrom,
                         LocalDate specialTo,
                         @NotNull ProductState state,
                         String brand,
                         List<String> categories,
                         @URL String imageUrl) {
}
