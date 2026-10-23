package com.shop.ecommerce.service;

import com.shop.ecommerce.dto.Dtos.*;
import com.shop.ecommerce.entity.Category;
import com.shop.ecommerce.exception.BadRequestException;
import com.shop.ecommerce.exception.ResourceNotFoundException;
import com.shop.ecommerce.repository.CategoryRepository;
import com.shop.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName())).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        String name = req.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }
        Category c = categoryRepository.save(Category.builder().name(name).build());
        return new CategoryResponse(c.getId(), c.getName());
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        String name = req.name().trim();
        if (!c.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }
        c.setName(name);
        return new CategoryResponse(c.getId(), c.getName());
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete a category that still has products");
        }
        categoryRepository.deleteById(id);
    }
}
