package com.order_service.client;

import com.order_service.dto.UserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("http://localhost:9000/api/user")
public interface UserServiceClient {

    @GetExchange("/{userId}")
    UserResponse findByUserId(
            @PathVariable("userId") String userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    );
}
