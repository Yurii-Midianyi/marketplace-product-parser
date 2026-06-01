package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.TestUtilities;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;
import com.ymidianyi.marketplace.product.parser.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(ProductImportService.class)
public class ProductImportServiceTest {

    @Autowired
    ProductImportService productImportService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @MockitoBean
    Clock mockClock;

    @Test
    void persistAllFieldsOnImportTest() {
        when(mockClock.instant()).thenReturn(Instant.parse("2026-05-22T20:07:51Z"));
        List<ProductDto> products = List.of(TestUtilities.createFullyPopulatedProduct());
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("products_ABC", mockClock.instant(), products);
        productImportService.importProducts(productExportFileDto, "products_PARTNER-A_2026-03-23.csv");

        Product savedProduct = productRepository.findBySkuAndPartnerId("5890", "products_ABC").orElseThrow(AssertionError::new);
        assertThat(savedProduct.getName()).isEqualTo("Milk");
        assertThat(savedProduct.getSku()).isEqualTo("5890");
        assertThat(savedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(534));
        assertThat(savedProduct.getSpecialPrice()).isEqualTo(BigDecimal.valueOf(400));
        assertThat(savedProduct.getSpecialFrom()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(savedProduct.getSpecialTo()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(savedProduct.getState()).isEqualTo(ProductState.ACTIVE);
        assertThat(savedProduct.getBrand()).isEqualTo("MilkyWay");
        assertThat(savedProduct.getImageUrl()).isEqualTo("https://nutritionsource.hsph.harvard.edu/milk/");
        assertThat(savedProduct.getCategories())
                .extracting("name")
                .containsExactlyInAnyOrder("Diary");
        assertThat(savedProduct.getImportedAt()).isEqualTo(mockClock.instant());
        assertThat(savedProduct.getPartnerId()).isEqualTo("products_ABC");
        assertThat(savedProduct.getSourceFileName()).isEqualTo("products_PARTNER-A_2026-03-23.csv");
    }

    @Test
    void updateExistingProductOnDuplicateSkuTest() {
        ProductDto updatedProductDto = new ProductDto(
                "Ice cream",
                "5890",
                BigDecimal.valueOf(545),
                BigDecimal.valueOf(420),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 2),
                ProductState.INACTIVE,
                "Choko",
                List.of("Sweets"),
                "https://nutritionsource.hsph.harvard.edu/icecream/"
        );
        when(mockClock.instant()).thenReturn(Instant.parse("2026-05-22T20:07:51Z"));
        List<ProductDto> products = List.of(updatedProductDto);
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("products_FrozenFactory", mockClock.instant(), products);
        productImportService.importProducts(productExportFileDto, "products_PARTNER-A_2026-03-23.csv");

        Product savedProduct = productRepository.findBySkuAndPartnerId("5890", "products_FrozenFactory").orElseThrow(AssertionError::new);
        assertThat(savedProduct.getName()).isEqualTo("Ice cream");
        assertThat(savedProduct.getSku()).isEqualTo("5890");
        assertThat(savedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(545));
        assertThat(savedProduct.getSpecialPrice()).isEqualTo(BigDecimal.valueOf(420));
        assertThat(savedProduct.getSpecialFrom()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(savedProduct.getSpecialTo()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(savedProduct.getState()).isEqualTo(ProductState.INACTIVE);
        assertThat(savedProduct.getBrand()).isEqualTo("Choko");
        assertThat(savedProduct.getImageUrl()).isEqualTo("https://nutritionsource.hsph.harvard.edu/icecream/");
        assertThat(savedProduct.getCategories())
                .extracting("name")
                .containsExactlyInAnyOrder("Sweets");
        assertThat(savedProduct.getImportedAt()).isEqualTo(mockClock.instant());
        assertThat(savedProduct.getPartnerId()).isEqualTo("products_FrozenFactory");
        assertThat(savedProduct.getSourceFileName()).isEqualTo("products_PARTNER-A_2026-03-23.csv");
    }

    @Test
    void reuseExistingCategoryAcrossProductsTest() {
        ProductDto updatedProductDto = new ProductDto(
                "Ice cream",
                "2222",
                BigDecimal.valueOf(545),
                BigDecimal.valueOf(420),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 2),
                ProductState.INACTIVE,
                "Choko",
                List.of("Diary"),
                "https://nutritionsource.hsph.harvard.edu/icecream/"
        );
        when(mockClock.instant()).thenReturn(Instant.parse("2026-05-22T20:07:51Z"));
        List<ProductDto> products = List.of(updatedProductDto, TestUtilities.createFullyPopulatedProduct());
        ProductExportFileDto productExportFileDto = new ProductExportFileDto("products_FrozenFactory", mockClock.instant(), products);
        productImportService.importProducts(productExportFileDto, "products_PARTNER-A_2026-03-23.csv");

        assertThat(productRepository.count()).isEqualTo(2);
        assertThat(categoryRepository.count()).isEqualTo(1);

    }
}
