package com.product_service.service;

import com.product_service.dto.CategoryRequest;
import com.product_service.dto.CategoryResponse;
import com.product_service.entities.Category;
import com.product_service.exception.ResourceNotFoundException;
import com.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest, String userId) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            log.error("Category name already exists");
            throw new ResourceNotFoundException("Category name already exists:" + categoryRequest.getName());
        }
        Category category = mapCategoryRequestToCategoryEntity(categoryRequest);
        category.setUserId(userId);
        Category savedCategory = categoryRepository.save(category);
        return mapCategoryEntityToCategoryResponse(savedCategory);
    }

    private CategoryResponse mapCategoryEntityToCategoryResponse(Category savedCategory) {
        return CategoryResponse.builder()
                .name(savedCategory.getName())
                .description(savedCategory.getDescription())
                .categoryId(savedCategory.getId())
                .build();
    }

    private Category mapCategoryRequestToCategoryEntity(CategoryRequest categoryRequest) {
        return Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();
    }

    @Override
    public List<CategoryResponse> getAllCategory() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::mapCategoryEntityToCategoryResponse).toList();
    }

    @Override
    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id"));
        return mapCategoryEntityToCategoryResponse(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id"));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id"));
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        return mapCategoryEntityToCategoryResponse(categoryRepository.save(category));
    }
}
