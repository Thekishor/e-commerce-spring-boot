package com.order_service.client;

import com.order_service.dto.PurchaseRequest;
import common.events.dto.PurchaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("http://localhost:9001/api/product")
public interface ProductServiceClient {

    @PostExchange("/purchase")
    public List<PurchaseResponse> purchaseResponses(
            @Valid @RequestBody List<PurchaseRequest> purchaseRequests,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") String roles
    );
}
