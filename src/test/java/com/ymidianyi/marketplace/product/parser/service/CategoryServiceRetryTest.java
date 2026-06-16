package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test for the concurrent-insert retry path in CategoryService.getOrCreate.
 * Uses mocks so no Spring context or DB is needed — the race scenario is simulated
 * by making findOrCreate throw DataIntegrityViolationException on the first call.
 */
class CategoryServiceRetryTest {

    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        // spy lets us fake the one method that needs a database (findOrCreate)
        // and keeping the method we actually want to test (getOrCreate) real.
        categoryService = spy(new CategoryService(categoryRepository));
    }

    @Test
    void getOrCreate_whenFindOrCreateThrowsDuplicate_fallsBackToFindByName() {
        Category existing = new Category("Fruits");
        doThrow(new DataIntegrityViolationException("duplicate key"))
                .when(categoryService).findOrCreate("Fruits");
        when(categoryRepository.findByName("Fruits")).thenReturn(Optional.of(existing));

        Category result = categoryService.getOrCreate("Fruits");

        assertThat(result).isSameAs(existing);
        verify(categoryService).findOrCreate("Fruits");
        verify(categoryRepository).findByName("Fruits");
    }

    @Test
    void getOrCreate_whenFindOrCreateSucceeds_fetchesViaFindByNameInCallerTransaction() {
        Category managed = new Category("Fruits");
        when(categoryRepository.findByName("Fruits")).thenReturn(Optional.of(managed));

        Category result = categoryService.getOrCreate("Fruits");

        assertThat(result).isSameAs(managed);
        verify(categoryService).findOrCreate("Fruits");
        // findByName is called twice: once inside findOrCreate (existence check)
        // and once in getOrCreate (to return a managed entity in the caller's transaction).
        verify(categoryRepository, times(2)).findByName("Fruits");
    }
}
