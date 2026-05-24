package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;
import com.ymidianyi.marketplace.product.parser.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public ProductImportService(ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                Clock clock) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    /** Holds file fields to pass through the logic */
    private record ImportContext(String partnerId, String sourceFileName) {}

    public void importProducts(ProductExportFileDto dto, String sourceFileName) {
        ImportContext context = new ImportContext(dto.partnerId(), sourceFileName);
        for (ProductDto productDto : dto.products()) {
            importSingleProduct(productDto, context);
        }
    }

    /**
     * Saves immediately so just created Category entities are flushed to the DB
     * before the next product's resolveCategories call runs its findByName query.
     * Fix for case when multiple products share a new category that does not exist in the DB yet:
     * the first product creates and saves it, and the next products find it instead of creating duplicates.
     */
    private void importSingleProduct(ProductDto dto, ImportContext context) {
        Product product = findOrCreateProduct(dto.sku(), context.partnerId());
        applyProductFields(product, dto, context);
        productRepository.save(product);
    }

    /**
     * Returns an existing Product for the given SKU + partner,
     * or a new instance if none exists yet - upsert logic.
     */
    private Product findOrCreateProduct(String sku, String partnerId) {
        return productRepository.findBySkuAndPartnerId(sku, partnerId)
                .orElseGet(Product::new);
    }

    /**
     * Copies fields from the DTO to the entity and replace categories.
     * Clears old category associations
     */
    private void applyProductFields(Product product, ProductDto dto, ImportContext context) {
        product.setName(dto.name());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setSpecialPrice(dto.specialPrice());
        product.setSpecialFrom(dto.specialFrom());
        product.setSpecialTo(dto.specialTo());
        product.setState(dto.state());
        product.setBrand(dto.brand());
        product.setImageUrl(dto.imageUrl());
        product.setPartnerId(context.partnerId());
        product.setSourceFileName(context.sourceFileName());
        product.setImportedAt(Instant.now(clock));

        product.replaceCategories(resolveCategories(dto.categories()));
    }

    /**
     * Looks up each category name in the database; creates and persists a new
     * Category if it does not exist yet. Blank, null, and duplicate names are skipped.
     * Cascade on the owning side (products) handles saving created instances.
     */
    private Set<Category> resolveCategories(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> new Category(name)))
                .collect(Collectors.toSet());
    }
}


