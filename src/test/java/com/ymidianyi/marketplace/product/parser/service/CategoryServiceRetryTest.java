package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CategoryServiceRetryTest {
    CategoryService categoryService;
    CategoryRepository categoryRepository;

    @BeforeEach
    void setUp(){
        categoryRepository = mock(CategoryRepository.class);
        categoryService = spy(new CategoryService(categoryRepository));
    }

    @Test
    void testDuplicateCategoryFound(){
        Category existing = new Category("Fruits");
        doThrow(new DataIntegrityViolationException("duplicate found")).when(categoryService).findOrCreate("Fruits");
        when(categoryRepository.findByName("Fruits")).thenReturn(Optional.of(existing));

        Category result = categoryService.getOrCreate("Fruits");
        assertThat(result).isSameAs(existing);
        verify(categoryService).findOrCreate("Fruits");
        verify(categoryRepository).findByName("Fruits");
    }

    @Test
    void testCategoryFound(){
        Category existing = new Category("Fruits");
        when(categoryRepository.findByName("Fruits")).thenReturn(Optional.of(existing));

        Category result = categoryService.getOrCreate("Fruits");
        assertThat(result).isSameAs(existing);
        verify(categoryService).findOrCreate("Fruits");
        verify(categoryRepository, times(2)).findByName("Fruits");
    }
}
