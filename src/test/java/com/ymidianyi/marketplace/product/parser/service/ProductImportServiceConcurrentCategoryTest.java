package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import com.ymidianyi.marketplace.product.parser.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProductImportServiceConcurrentCategoryTest {

    private ProductRepository productRepository;
    private CategoryService categoryService;
    private ProductImportService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryService   = mock(CategoryService.class);

        service = new ProductImportService(
                productRepository, categoryService,
                Clock.fixed(Instant.parse("2026-03-23T10:00:00Z"), ZoneOffset.UTC));

        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findBySkuAndPartnerId(anyString(), anyString()))
                .thenReturn(Optional.of(new Product()));
    }

    @Test
    void importProducts_delegatesCategoryResolutionToService() {
        when(categoryService.getOrCreate("Fruits")).thenReturn(new Category("Fruits"));

        ProductDto product = new ProductDto(
                "Apple", "SKU-1", BigDecimal.TEN, null, null, null,
                ProductState.ACTIVE, null, List.of("Fruits"), null);

        service.importProducts(
                new ProductExportFileDto("PARTNER-A", Instant.now(), List.of(product)),
                "file.json");

        verify(categoryService).getOrCreate("Fruits");
    }

    @Test
    void importProducts_deduplicatesCategoryNamesBeforeCallingService() {
        when(categoryService.getOrCreate(anyString())).thenReturn(new Category("Fruits"));

        ProductDto product = new ProductDto(
                "Apple", "SKU-1", BigDecimal.TEN, null, null, null,
                ProductState.ACTIVE, null, List.of("Fruits", "Fruits"), null);

        service.importProducts(
                new ProductExportFileDto("PARTNER-A", Instant.now(), List.of(product)),
                "file.json");

        verify(categoryService, times(1)).getOrCreate("Fruits");
    }
}
