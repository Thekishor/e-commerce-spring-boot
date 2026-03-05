package com.product_service.controller;

import com.product_service.dto.*;
import com.product_service.interceptor.UserContext;
import com.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private static final List<String> CREATOR_ROLES = List.of("ADMIN", "CREATOR");
    private static final List<String> REQUIRED_ROLES = List.of("ADMIN");
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest
    ) {
        if (CREATOR_ROLES.stream().anyMatch(UserContext.getUserRole()::contains)) {
            ProductResponse response = productService.createProduct(productRequest);
            log.info("Product created information: {}", response);
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Product created successfully")
                    .status(true)
                    .instant(Instant.now())
                    .build(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("You do not have permission to create product request")
                    .status(false)
                    .instant(Instant.now())
                    .build(), HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<List<PurchaseResponse>> purchaseResponses(
            @Valid @RequestBody List<PurchaseRequest> purchaseRequests
    ) {
        List<PurchaseResponse> purchaseResponses = productService.createPurchase(purchaseRequests);
        return new ResponseEntity<>(purchaseResponses, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<ProductResponse>>> getAllProduct(
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Sort sort = null;
        if (sortDir.equalsIgnoreCase("ASC")) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }

        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Map<String, List<ProductResponse>> product = productService.getAllProduct(pageRequest, search);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Integer id) {
        ProductResponse productResponse = productService.getProductByID(id);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpResponse> deleteProduct(
            @PathVariable("id") Integer id
    ) {
        if (REQUIRED_ROLES.stream().allMatch(UserContext.getUserRole()::contains)) {
            productService.deleteProduct(id);
            log.info("Product deleted successfully");
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Product deleted successfully")
                    .status(true)
                    .instant(Instant.now())
                    .build(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpResponse.builder()
                .message("Only Admin can delete the product")
                .status(false)
                .instant(Instant.now())
                .build(), HttpStatus.FORBIDDEN);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpResponse> updateProduct(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProductRequest productRequest
    ) {
        if (REQUIRED_ROLES.stream().allMatch(UserContext.getUserRole()::contains)) {
            ProductResponse productResponse = productService.updateProduct(id, productRequest);
            log.info("Product updated successfully: {}", productResponse);
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Product updated successfully")
                    .status(true)
                    .instant(Instant.now())
                    .build(), HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(HttpResponse.builder()
                    .message("Only Admin can update the product information")
                    .status(false)
                    .instant(Instant.now())
                    .build(), HttpStatus.FORBIDDEN);
        }
    }
}
