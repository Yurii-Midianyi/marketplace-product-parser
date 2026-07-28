package com.ymidianyi.marketplace.product.parser.service;

import com.ymidianyi.marketplace.product.parser.model.Category;
import com.ymidianyi.marketplace.product.parser.repository.CategoryRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryInsertService categoryInsertService;

    public CategoryService(CategoryRepository categoryRepository, CategoryInsertService categoryInsertService) {
        this.categoryRepository = categoryRepository;
        this.categoryInsertService = categoryInsertService;
    }

    public Category getOrCreate(String name){
        try{
            categoryInsertService.findOrCreate(name);
        } catch (DataIntegrityViolationException ex){
            log.debug("Category '{}' was inserted concurrently, fetching existing row", name);
        }
        return categoryRepository.findByName(name).orElseThrow(()->new IllegalStateException("Category '"+name+"' was not found"));
    }

}
