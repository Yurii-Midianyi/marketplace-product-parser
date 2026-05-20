package com.ymidianyi.marketplace.product.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import java.math.BigDecimal;

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

    public static ProductDto createNotValidProduct() {
        return new ProductDto(
                null,
                "5645",
                BigDecimal.valueOf(-500),
                null,
                null,
                null,
                ProductState.ACTIVE,
                null,
                null,
                null
        );
    }
}

