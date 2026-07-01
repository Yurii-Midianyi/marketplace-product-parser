package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;
import com.ymidianyi.marketplace.product.parser.repository.ProductRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ProductImportService.class, CategoryService.class, CategoryInsertService.class})
// CategoryInsertService.findOrCreate uses REQUIRES_NEW — those transactions commit independently
// and are NOT rolled back by @DataJpaTest's default rollback mechanism.
// NOT_SUPPORTED disables the outer test transaction; @AfterEach handles cleanup explicitly.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductImportServiceTest {

    static final Instant FIXED_NOW = Instant.parse("2026-03-23T10:00:00Z");

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        }
    }

    @Autowired ProductImportService service;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    // Runs assertion blocks inside a tiny transaction so Hibernate can initialize
    // lazy collections (for example, saved.getCategories()) after importProducts() ends.
    @Autowired TransactionTemplate transactionTemplate;

    @AfterEach
    void cleanup() {
        // Products must be deleted before categories to avoid FK constraint violations
        // on the product_categories join table.
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldPersistAllFieldsOnFirstImport() {
        service.importProducts(exportDto(List.of(fullProduct())), "products_PARTNER-A_2026-03-23.json");

        transactionTemplate.executeWithoutResult(status -> {
            Product saved = productRepository
                    .findBySkuAndPartnerId("802999", "PARTNER-A")
                    .orElseThrow();

            assertThat(saved.getName()).isEqualTo("Apple Fruit");
            assertThat(saved.getPrice()).isEqualByComparingTo("41238.0");
            assertThat(saved.getSpecialPrice()).isEqualByComparingTo("35000.0");
            assertThat(saved.getSpecialFrom()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(saved.getSpecialTo()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(saved.getState()).isEqualTo(ProductState.ACTIVE);
            assertThat(saved.getBrand()).isEqualTo("Shelf 3");
            assertThat(saved.getImageUrl()).isEqualTo("https://img.example.com/apple.jpg");
            assertThat(saved.getPartnerId()).isEqualTo("PARTNER-A");
            assertThat(saved.getSourceFileName()).isEqualTo("products_PARTNER-A_2026-03-23.json");
            assertThat(saved.getImportedAt()).isEqualTo(FIXED_NOW);
            assertThat(saved.getCategories())
                    .extracting("name")
                    .containsExactlyInAnyOrder("Golden apple Bundles");
        });
    }

    @Test
    void shouldUpdateExistingProductOnDuplicateSku() {
        service.importProducts(exportDto(List.of(fullProduct())), "first.json");

        ProductDto updated = new ProductDto(
                "Apple Fruit v2", "802999", new BigDecimal("50000.0"),
                null, null, null, ProductState.INACTIVE, "New Brand",
                List.of("Seasonal"), "https://img.example.com/apple2.jpg");
        service.importProducts(exportDto(List.of(updated)), "second.json");

        assertThat(productRepository.count()).isEqualTo(1);

        transactionTemplate.executeWithoutResult(status -> {
            Product saved = productRepository
                    .findBySkuAndPartnerId("802999", "PARTNER-A")
                    .orElseThrow();

            assertThat(saved.getName()).isEqualTo("Apple Fruit v2");
            assertThat(saved.getPrice()).isEqualByComparingTo("50000.0");
            assertThat(saved.getState()).isEqualTo(ProductState.INACTIVE);
            assertThat(saved.getSpecialPrice()).isNull();
            assertThat(saved.getSourceFileName()).isEqualTo("second.json");
            assertThat(saved.getCategories())
                    .extracting("name")
                    .containsExactlyInAnyOrder("Seasonal");
        });
    }

    @Test
    void shouldReuseExistingCategoryAcrossProducts() {
        ProductDto product1 = new ProductDto(
                "Product One", "SKU-001", new BigDecimal("100.0"),
                null, null, null, ProductState.ACTIVE, null, List.of("Fruits"), null);
        ProductDto product2 = new ProductDto(
                "Product Two", "SKU-002", new BigDecimal("200.0"),
                null, null, null, ProductState.ACTIVE, null, List.of("Fruits"), null);

        service.importProducts(exportDto(List.of(product1, product2)), "products.json");

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(productRepository.count()).isEqualTo(2);
    }

    private ProductDto fullProduct() {
        return new ProductDto(
                "Apple Fruit", "802999", new BigDecimal("41238.0"), new BigDecimal("35000.0"),
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1),
                ProductState.ACTIVE, "Shelf 3",
                List.of("Golden apple Bundles"), "https://img.example.com/apple.jpg");
    }

    private ProductExportFileDto exportDto(List<ProductDto> products) {
        return new ProductExportFileDto("PARTNER-A", Instant.parse("2026-03-23T10:30:00Z"), products);
    }
}
