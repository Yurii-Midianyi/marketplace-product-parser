package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles category INSERT in its own Spring bean to ensure {@code @Transactional} is applied
 * through the proxy when called from {@link CategoryService}.
 */
@Service
public class CategoryInsertService {

    private final CategoryRepository categoryRepository;

    public CategoryInsertService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Inserts the category if it does not exist yet, in a dedicated transaction.
     * REQUIRES_NEW ensures this transaction is independent: a DataIntegrityViolationException
     * from a concurrent duplicate insert rolls back only this transaction, not the caller's.
     * saveAndFlush forces the INSERT immediately so the violation is raised here, not at
     * the outer transaction's commit boundary where it can no longer be caught.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Category findOrCreate(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.saveAndFlush(new Category(name)));
    }
}
