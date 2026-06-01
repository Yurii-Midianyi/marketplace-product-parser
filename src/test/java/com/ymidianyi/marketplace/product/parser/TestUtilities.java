package com.ymidianyi.marketplace.product.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TestUtilities {

    public static ProductDto createCheeseProduct() {
        return new ProductDto(
                "Cheese",
                "5645",
                BigDecimal.valueOf(500),
                null,
                null,
                null,
                ProductState.ACTIVE,
                null,
                null,
                null
        );
    }

    /*
    empty name,
    negative price,
    special price is present but special dates are missing
    */
    public static ProductDto createNotValidProduct() {
        return new ProductDto(
                null,
                "5645",
                BigDecimal.valueOf(-500),
                BigDecimal.valueOf(1000),
                null,
                null,
                ProductState.ACTIVE,
                null,
                null,
                null
        );
    }

    public static ProductDto createFullyPopulatedProduct() {
        return new ProductDto(
                "Milk",
                "5890",
                BigDecimal.valueOf(534),
                BigDecimal.valueOf(400),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 8),
                ProductState.ACTIVE,
                "MilkyWay",
                List.of("Diary"),
                "https://nutritionsource.hsph.harvard.edu/milk/"
        );
    }
}

