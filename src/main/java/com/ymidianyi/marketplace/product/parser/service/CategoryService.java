package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryInsertService categoryInsertService;

    public CategoryService(CategoryRepository categoryRepository,
                           CategoryInsertService categoryInsertService) {
        this.categoryRepository = categoryRepository;
        this.categoryInsertService = categoryInsertService;
    }

    /**
     * Returns a managed Category entity for the given name, creating the row if needed.
     * encapsulates the full logic:
     * ensure the row exists, then return an entity bound to the caller's session.
     *
     * Steps:
     * 1. categoryInsertService.findOrCreate (REQUIRES_NEW) inserts the row in its own
     *    isolated transaction. If two threads race on the same name, the loser's transaction
     *    rolls back on its own without breaking the product-insert transaction, and execution
     *    falls through to step 2.
     * 2. categoryRepository.findByName runs in the caller's transaction (REQUIRED, the
     *    default), so the returned entity is managed by the caller's EntityManager —
     *    safe to pass into product.replaceCategories() without detached-entity errors.
     */
    public Category getOrCreate(String name) {
        try {
            categoryInsertService.findOrCreate(name);
        } catch (DataIntegrityViolationException e) {
            log.debug("Category '{}' was inserted concurrently, fetching existing row", name);
        }
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Category '" + name + "' not found after findOrCreate"));
    }
}
