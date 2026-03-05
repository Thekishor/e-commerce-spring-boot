package com.product_service.controller;

import com.product_service.dto.CategoryRequest;
import com.product_service.dto.CategoryResponse;
import com.product_service.dto.HttpResponse;
import com.product_service.interceptor.UserContext;
import com.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private static final List<String> CREATOR_ROLES = List.of("ADMIN", "CREATOR");
    private static final List<String> REQUIRED_ROLES = List.of("ADMIN");
    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        log.info("Category name: {}", categoryRequest.getName());

        if (CREATOR_ROLES.stream().anyMatch(UserContext.getUserRole()::contains)) {
            CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);
            log.info("Category created successfully: {}", categoryResponse.getName());
            return new ResponseEntity<>(
                    HttpResponse.builder()
                            .message("Category created successfully")
                            .status(true)
                            .instant(Instant.now())
                            .build(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(
                    HttpResponse.builder()
                            .message("You do not have permission to create category request")
                            .status(false)
                            .instant(Instant.now())
                            .build(),
                    HttpStatus.FORBIDDEN
            );
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
    public ResponseEntity<HttpResponse> deleteCategory(
            @PathVariable("id") Integer id
    ) {
        if (REQUIRED_ROLES.stream().allMatch(UserContext.getUserRole()::contains)) {
            categoryService.deleteCategory(id);
            log.info("Category deleted successfully: {} ", UserContext.getUserId());
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Category created successfully")
                    .status(true)
                    .instant(Instant.now())
                    .build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("You do not have permission to delete the category")
                    .status(false)
                    .instant(Instant.now())
                    .build(), HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpResponse> updateCategory(
            @PathVariable("id") Integer id,
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        if (REQUIRED_ROLES.stream().allMatch(UserContext.getUserRole()::contains)) {
            boolean updated = categoryService.updateCategory(id, categoryRequest);
            if (updated) {
                log.info("Category updated successfully by user: {}", UserContext.getUserId());
                return new ResponseEntity<>(HttpResponse.builder()
                        .message("Category updated successfully")
                        .status(true)
                        .instant(Instant.now())
                        .build(), HttpStatus.OK);
            } else {
                log.error("Unable to updated category");
                return new ResponseEntity<>(HttpResponse.builder()
                        .message("Unable to updated category")
                        .status(false)
                        .instant(Instant.now())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Only Admin can update the category information")
                    .status(false)
                    .instant(Instant.now())
                    .build(), HttpStatus.FORBIDDEN);
        }
    }
}