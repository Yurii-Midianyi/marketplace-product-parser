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

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getOrCreate(String name){
        try{
            findOrCreate(name);
        } catch (DataIntegrityViolationException ex){
            log.debug("Category '{}' was inserted concurrently, fetching existing row", name);
        }
        return categoryRepository.findByName(name).orElseThrow(()->new IllegalStateException("Category '"+name+"' was not found"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Category findOrCreate(String name){
        return categoryRepository.findByName(name).orElseGet(()->categoryRepository.saveAndFlush(new Category(name)));
    }
}
