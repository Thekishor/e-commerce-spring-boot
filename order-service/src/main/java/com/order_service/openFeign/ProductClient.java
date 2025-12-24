package com.order_service.openFeign;

import com.order_service.dto.PurchaseRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import common.events.kafkaEvents.PurchaseResponse;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @PostMapping("/api/product/purchase")
    public List<PurchaseResponse> purchaseResponses(
            @Valid @RequestBody List<PurchaseRequest> purchaseRequests,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    );
}
