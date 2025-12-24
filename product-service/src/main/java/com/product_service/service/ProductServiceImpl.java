package com.product_service.service;

import com.product_service.dto.ProductRequest;
import com.product_service.dto.ProductResponse;
import com.product_service.dto.PurchaseRequest;
import com.product_service.dto.PurchaseResponse;
import com.product_service.entities.Category;
import com.product_service.entities.Product;
import com.product_service.exception.ResourceNotFoundException;
import com.product_service.repository.CategoryRepository;
import com.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductRequest productRequest, String userId) {
        if (!categoryRepository.existsById(productRequest.getCategoryId())) {
            log.error("Category not found with id");
            throw new ResourceNotFoundException("Category not found with id");
        }
        Product product = mapProductRequestToProductEntity(productRequest);
        product.setUserId(userId);
        Product savedProduct = productRepository.save(product);
        return mapProductEntityToProductResponse(savedProduct);
    }

    @Transactional
    @Override
    public List<PurchaseResponse> createPurchase(List<PurchaseRequest> purchaseRequest) {
        var productId = purchaseRequest.stream().map(PurchaseRequest::getProductId).toList();

        var availableProduct = productRepository.findAllByIdInOrderById(productId);

        if (productId.size() != availableProduct.size()) {
            throw new ResourceNotFoundException("One or More products does not exists");
        }

        var sortedProductRequest =
                purchaseRequest.stream().sorted(Comparator.comparing(PurchaseRequest::getProductId)).toList();

        List<PurchaseResponse> purchaseResponses = new ArrayList<>();

        for (int i = 0; i < sortedProductRequest.size(); i++) {
            var product = availableProduct.get(i);
            var productRequest = sortedProductRequest.get(i);

            if (product.getQuantity() < productRequest.getQuantity()) {
                throw new ResourceNotFoundException("Insufficient stock " + "quantity for product with Id: " + productRequest.getProductId());
            }
            var newQuantity = product.getQuantity() - productRequest.getQuantity();
            product.setQuantity(newQuantity);

            purchaseResponses.add(mapProductEntityToPurchaseResponse(product, productRequest));
        }
        productRepository.saveAll(availableProduct);

        return purchaseResponses;
    }

    private PurchaseResponse mapProductEntityToPurchaseResponse(Product product, PurchaseRequest purchaseRequest) {
        return PurchaseResponse.builder()
                .productId(product.getId())
                .quantity(purchaseRequest.getQuantity())
                .price(product.getPrice() * purchaseRequest.getQuantity())
                .description(product.getDescription())
                .name(product.getName())
                .build();
    }

    private ProductResponse mapProductEntityToProductResponse(Product savedProduct) {
        return ProductResponse.builder()
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .description(savedProduct.getDescription())
                .quantity(savedProduct.getQuantity())
                .categoryName(savedProduct.getCategory().getName())
                .build();

    }

    private Product mapProductRequestToProductEntity(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id"));
        return Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .category(category)
                .build();
    }

    @Override
    public ProductResponse getProductByID(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id"));
        return mapProductEntityToProductResponse(product);
    }

    @Override
    public Map<String, List<ProductResponse>> getAllProduct() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = products
                .stream().map(this::mapProductEntityToProductResponse).toList();
        return productResponses.stream().collect(Collectors.groupingBy(ProductResponse::getCategoryName));
    }

    @Override
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id"));
        productRepository.delete(product);
    }

    @Override
    public ProductResponse updateProduct(Integer id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id"));
        if (!categoryRepository.existsById(productRequest.getCategoryId())) {
            log.error("Category not found");
            throw new ResourceNotFoundException("Category not found with id");
        }
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        Product savedProduct = productRepository.save(product);
        return mapProductEntityToProductResponse(savedProduct);
    }
}
