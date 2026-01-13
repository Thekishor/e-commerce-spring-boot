package com.product_service.controller;

import com.product_service.dto.ProductRequest;
import com.product_service.dto.ProductResponse;
import com.product_service.dto.PurchaseRequest;
import com.product_service.dto.PurchaseResponse;
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

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductRequest productRequest,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") List<String> roles
    ) {
        if (roles.contains("ADMIN") || roles.contains("CREATOR")) {
            ProductResponse response = productService.createProduct(productRequest, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "You do not have permission to create product request",
                    "status", "false",
                    "timeStamp", Instant.now()
            ), HttpStatus.FORBIDDEN);
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
    public ResponseEntity<?> deleteProduct(
            @PathVariable("id") Integer id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") List<String> roles
    ) {
        if (roles.contains("ADMIN")) {
            productService.deleteProduct(id);
            log.info("Product deleted by user: {} {}", userId, roles);
            return new ResponseEntity<>(Map.of(
                    "message", "Product deleted successfully"
            ), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(Map.of(
                "message", "Only Admin can delete the product",
                "status", "false",
                "timeStamp", Instant.now()
        ), HttpStatus.FORBIDDEN);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProductRequest productRequest,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") List<String> roles
    ) {
        if (roles.contains("ADMIN")) {
            ProductResponse productResponse = productService.updateProduct(id, productRequest);
            log.info("Product updated by user: {} {}", userId, roles);
            return new ResponseEntity<>(productResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "Only Admin can update the product information",
                    "status", "false",
                    "timeStamp", Instant.now()
            ), HttpStatus.FORBIDDEN);
        }
    }
}
