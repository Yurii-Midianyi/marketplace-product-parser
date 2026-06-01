package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;
import com.ymidianyi.marketplace.product.parser.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductImportService {

    private final Clock clock;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductImportService(Clock clock, ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.clock = clock;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public void importProducts(ProductExportFileDto productExportFileDto, String sourceFileName) {
        for (ProductDto productDto : productExportFileDto.products()) {
            importSingleProduct(productDto, sourceFileName, productExportFileDto.partnerId());
        }
    }

    private void importSingleProduct(ProductDto dto, String sourceFileName, String partnerId){
        Product product = findOrCreateProduct(dto.sku(), partnerId);
        mapProductFields(product, dto, sourceFileName, partnerId);
        productRepository.save(product);
    }

    private Product findOrCreateProduct(String sku, String partnerId) {
        return productRepository.findBySkuAndPartnerId(sku, partnerId).orElseGet(Product::new);
    }

    private void mapProductFields(Product product, ProductDto dto, String sourceFileName, String partnerId){
            product.setSku(dto.sku());
            product.setName(dto.name());
            product.setPrice(dto.price());
            product.setSpecialPrice(dto.specialPrice());
            product.setSpecialFrom(dto.specialFrom());
            product.setSpecialTo(dto.specialTo());
            product.setState(dto.state());
            product.setBrand(dto.brand());
            product.setImageUrl(dto.imageUrl());
            product.setPartnerId(partnerId);
            product.setSourceFileName(sourceFileName);
            product.setImportedAt(clock.instant());
            product.replaceCategories(resolveCategories(dto.categories()));
    }

    private Set<Category> resolveCategories(List<String> names){
        if(names == null || names.isEmpty()){
            return Collections.emptySet();
        }
        return names.stream()
                .filter(name-> name != null && !name.isBlank())
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> new Category(name)))
                .collect(Collectors.toSet());

    }
}
