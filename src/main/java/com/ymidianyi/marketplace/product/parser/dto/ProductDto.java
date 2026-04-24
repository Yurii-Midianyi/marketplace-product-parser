package com.ymidianyi.marketplace.product.parser.dto;

import com.ymidianyi.marketplace.product.parser.model.ProductState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductDto(String name,
                         String sku,
                         BigDecimal price,
                         BigDecimal specialPrice,
                         LocalDate specialFrom,
                         LocalDate specialTo,
                         ProductState state,
                         String brand,
                         List<String> categories,
                         String imageUrl) {
}
