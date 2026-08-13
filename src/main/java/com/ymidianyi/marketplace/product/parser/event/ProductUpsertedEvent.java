package com.ymidianyi.marketplace.product.parser.event;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Published once for every product persisted (inserted or updated) by an import
 * Key {@code partnerId|sku}
 */
public record ProductUpsertedEvent (
        String eventId,
        Instant occurredAt,
        String partnerId,
        String sku,
        String name,
        BigDecimal price,
        BigDecimal specialPrice,
        LocalDate specialFrom,
        LocalDate specialTo,
        ProductState state,
        String brand,
        String imageUrl,
        List<String> categories,
        String sourceFileName,
        Instant importedAt){

    public static ProductUpsertedEvent from(Product product, Instant occurredAt){
        List<String> categoryNames = product.getCategories().stream()
                .map(Category::getName)
                .sorted()
                .toList();
        return new ProductUpsertedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                product.getPartnerId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getSpecialPrice(),
                product.getSpecialFrom(),
                product.getSpecialTo(),
                product.getState(),
                product.getBrand(),
                product.getImageUrl(),
                categoryNames,
                product.getSourceFileName(),
                product.getImportedAt()
        );
    }

    public String messageKey(){
        return partnerId + "|" + sku;
    }

}
