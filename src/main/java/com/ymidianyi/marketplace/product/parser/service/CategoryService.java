package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Returns a managed Category entity for the given name, creating the row if needed.
     * encapsulates the full logic:
     * ensure the row exists, then return an entity bound to the caller's session.
     *
     * Steps:
     * 1. findOrCreate (REQUIRES_NEW) inserts the row in its own isolated transaction.
     *    If two threads race on the same name, the loser's transaction rolls back on its
     *    own without breaking the product-insert transaction, and execution falls through to step 2.
     * 2. categoryRepository.findByName runs in the caller's transaction (REQUIRED, the
     *    default), so the returned entity is managed by the caller's EntityManager —
     *    safe to pass into product.replaceCategories() without detached-entity errors.
     */
    public Category getOrCreate(String name) {
        try {
            findOrCreate(name);
        } catch (DataIntegrityViolationException e) {
            log.debug("Category '{}' was inserted concurrently, fetching existing row", name);
        }
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Category '" + name + "' not found after findOrCreate"));
    }

    /**
     * Inserts the category if it does not exist yet, in a dedicated transaction.
     * Package-private — callers should use getOrCreate() which handles the full lifecycle.
     *
     * REQUIRES_NEW ensures this transaction is independent: a DataIntegrityViolationException
     * from a concurrent duplicate insert rolls back only this transaction, not the caller's.
     * saveAndFlush forces the INSERT immediately so the violation is raised here, not at
     * the outer transaction's commit boundary where it can no longer be caught.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Category findOrCreate(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.saveAndFlush(new Category(name)));
    }
}
