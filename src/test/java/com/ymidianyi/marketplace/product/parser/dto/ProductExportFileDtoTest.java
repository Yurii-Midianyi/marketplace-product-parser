package com.ymidianyi.marketplace.product.parser.dto;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ProductExportFileDtoTest {

    @Autowired
    private JacksonTester<ProductExportFileDto> json;

    @Test
    void testDeserialization() throws IOException {
        String content = """
            {
                "partnerId": "123",
                "exportDate": "2026-04-17T18:00:00z"
            }
            """;
        ProductExportFileDto result = json.parseObject(content);

        assertEquals("123", result.partnerId());
        assertEquals(Instant.parse("2026-04-17T18:00:00z"), result.exportDate());
    }

    @Test
    void testSerialization() throws IOException {
        ProductDto firstProduct = new ProductDto("test1", "aaa", BigDecimal.ONE, new BigDecimal(55), LocalDate.now(), null, null, null, null, null);
        List<ProductDto> products = new ArrayList<>(Collections.singleton(firstProduct));
        Instant instant = Instant.parse("2026-04-17T18:00:00Z");
        ProductExportFileDto dto = new ProductExportFileDto("123", instant, products);

        String expectedJson = """
            {
                "partnerId": "123",
                "exportDate": "2026-04-17T18:00:00Z",
                "products": [
                    {
                        "name": "test1",
                        "sku": "aaa"
                    }
                ]
            }
            """;
        assertThat(json.write(dto)).isEqualToJson(expectedJson);
    }

}
