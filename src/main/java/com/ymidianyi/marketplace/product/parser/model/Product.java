package com.ymidianyi.marketplace.product.parser.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private BigDecimal specialPrice;
    private LocalDate specialFrom;
    private LocalDate specialTo;
    private ProductState state;
    private String brand;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "categoryId")
    )
    private Set<Category> categories;
    private String imageUrl;
    private String partnerId;
    private String sourceFileName;
    private Instant importedAt;
}
