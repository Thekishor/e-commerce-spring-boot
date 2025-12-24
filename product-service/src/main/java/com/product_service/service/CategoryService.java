package com.product_service.service;

import com.product_service.dto.CategoryRequest;
import com.product_service.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest, String userId);

    List<CategoryResponse> getAllCategory();

    CategoryResponse getCategoryById(Integer id);

    void deleteCategory(Integer id);

    CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest);
}
