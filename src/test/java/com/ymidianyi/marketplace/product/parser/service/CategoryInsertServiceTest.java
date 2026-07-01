package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategoryInsertService.class)
// CategoryInsertService.findOrCreate uses REQUIRES_NEW which commits independently of any
// outer transaction. @DataJpaTest wraps each test in a rollback transaction by default,
// but that rollback does NOT undo REQUIRES_NEW commits — so data is dirty between tests.
// NOT_SUPPORTED disables the outer test transaction; @AfterEach handles cleanup instead.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CategoryInsertServiceTest {

    @Autowired
    CategoryInsertService categoryInsertService;

    @Autowired
    CategoryRepository categoryRepository;

    @AfterEach
    void cleanup() {
        categoryRepository.deleteAll();
    }

    @Test
    void findOrCreate_newCategory_persistsAndReturnsIt() {
        var result = categoryInsertService.findOrCreate("Fruits");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Fruits");
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    void findOrCreate_existingCategory_returnsExistingWithoutDuplicate() {
        categoryInsertService.findOrCreate("Fruits");

        var result = categoryInsertService.findOrCreate("Fruits");

        assertThat(result.getName()).isEqualTo("Fruits");
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    void findOrCreate_twoDifferentNames_createsBoth() {
        categoryInsertService.findOrCreate("Fruits");
        categoryInsertService.findOrCreate("Vegetables");

        assertThat(categoryRepository.count()).isEqualTo(2);
    }
}
