package com.ymidianyi.marketplace.product.parser.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "categories")
@EqualsAndHashCode(of = {"sku", "partnerId"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal specialPrice;

    private LocalDate specialFrom;

    private LocalDate specialTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState state;

    private String brand;

    private String imageUrl;

    @Column(nullable = false)
    private String partnerId;

    @Column(nullable = false)
    private String sourceFileName;

    @Column(nullable = false)
    private Instant importedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    public void addCategory(Category category) {
        categories.add(category);
        category.getProducts().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getProducts().remove(this);
    }
}
