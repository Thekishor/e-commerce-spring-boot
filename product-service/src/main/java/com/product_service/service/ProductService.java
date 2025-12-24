package com.product_service.service;

import com.product_service.dto.ProductRequest;
import com.product_service.dto.ProductResponse;
import com.product_service.dto.PurchaseRequest;
import com.product_service.dto.PurchaseResponse;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductResponse createProduct(ProductRequest productRequest, String userId);

    List<PurchaseResponse> createPurchase(List<PurchaseRequest> purchaseRequest);

    ProductResponse getProductByID(Integer id);

    Map<String, List<ProductResponse>> getAllProduct();

    void deleteProduct(Integer id);

    ProductResponse updateProduct(Integer id, ProductRequest productRequest);
}
