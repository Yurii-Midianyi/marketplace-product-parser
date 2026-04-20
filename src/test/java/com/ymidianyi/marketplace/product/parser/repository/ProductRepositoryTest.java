package com.ymidianyi.marketplace.product.parser.repository;

import com.ymidianyi.marketplace.product.parser.model.Product;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest{

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndReadProduct() {
        // 1. Create your entity
        Product product = new Product();
        product.setName("Gaming Mouse");
        product.setSku("default");
        product.setPrice(new BigDecimal("55.00"));
        product.setState(ProductState.ACTIVE);

        // 2. Persist to H2 using TestEntityManager
        // persistAndFlush returns the managed entity with the generated ID
        Product savedProduct = entityManager.persistAndFlush(product);

        // 3. Clear the persistence context
        // This forces the next "find" to actually hit the H2 database
        // instead of just reading from the Hibernate cache.
        entityManager.clear();

        // 4. Read it back
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // 5. Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Gaming Mouse");
        assertThat(foundProduct.get().getSku()).isEqualTo("default");
        assertThat(foundProduct.get().getState()).isEqualTo(ProductState.ACTIVE);
    }
}
