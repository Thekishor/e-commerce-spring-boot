package com.product_service.controller;

import com.product_service.dto.CategoryRequest;
import com.product_service.dto.CategoryResponse;
import com.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (role.equals("ADMIN")) {
            CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest, userId);
            return new ResponseEntity<>(categoryResponse, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "You do not have permission to create category request",
                    "status", "false",
                    "timeStamp", Instant.now()
            ), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategory() {
        List<CategoryResponse> categoryResponses = categoryService.getAllCategory();
        return new ResponseEntity<>(categoryResponses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Integer id) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable("id") Integer id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        if (role.equals("ADMIN")) {
            categoryService.deleteCategory(id);
            log.info("Category deleted by user: {} {} ", email, role);
            return new ResponseEntity<>(Map.of(
                    "message", "Category Deleted Successfully"
            ), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "You do not have permission to delete the category",
                    "status", "false",
                    "timeStamp", Instant.now()
            ), HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable("id") Integer id,
            @Valid @RequestBody CategoryRequest categoryRequest,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        if (role.equals("ADMIN")) {
            CategoryResponse categoryResponse = categoryService.updateCategory(id, categoryRequest);
            log.info("Category updated successfully by user: {} {}", email, role);
            return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "Only Admin can update the category information",
                    "status", "false",
                    "timeStamp", Instant.now()
            ), HttpStatus.FORBIDDEN);
        }
    }
}