package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(CategoryService.class)
// CategoryService.findOrCreate uses REQUIRES_NEW which commits independently of any
// outer transaction. @DataJpaTest wraps each test in a rollback transaction by default,
// but that rollback does NOT undo REQUIRES_NEW commits — so data is dirty between tests.
// NOT_SUPPORTED disables the outer test transaction; @AfterEach handles cleanup instead.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @AfterEach
    void cleanUp(){
        categoryRepository.deleteAll();
    }

    @Test
    public void testNewCategoryCreated(){
        Category category = categoryService.getOrCreate("PC parts");
        assertThat(category.getName()).isEqualTo("PC parts");
        assertThat(category.getCategoryId()).isNotNull();
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    public void testFindExistingCategory(){
        categoryService.getOrCreate("PC parts");
        Category category = categoryService.getOrCreate("PC parts");

        assertThat(category.getName()).isEqualTo("PC parts");
        assertThat(category.getCategoryId()).isNotNull();
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    public void testCreateMultipleCategories(){
        categoryService.getOrCreate("PC parts");
        categoryService.getOrCreate("Electronics");

        assertThat(categoryRepository.count()).isEqualTo(2);
    }
}
