package com.order_service.controller;

import com.order_service.dto.OrderLineResponse;
import com.order_service.dto.OrderRequest;
import com.order_service.dto.OrderResponse;
import com.order_service.exception.ResourceNotFoundException;
import com.order_service.exception.UnauthorizedException;
import com.order_service.service.OrderLineService;
import com.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderLineService orderLineService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                OrderResponse orderResponse = orderService.createOrder(orderRequest, authHeader, userId);
                return new ResponseEntity<>(Map.of(
                        "message", "Order created successfully"
                ), HttpStatus.CREATED);
            } catch (Exception exception) {
                log.error("Error occurs during order creating: {}", exception.getMessage());
                throw new ResourceNotFoundException("Error occurs during order creating");
            }
        } else {
            log.error("Authorization header missing or invalid");
            throw new UnauthorizedException("Authorization header missing or invalid");
        }
    }

    @GetMapping
    public ResponseEntity<?> findAllOrders(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        if (role.equals("ADMIN")) {
            List<OrderResponse> orderResponses = orderService.getAllOrders();
            log.info("Order responses details seen by user: {} {}", email, role);
            return new ResponseEntity<>(orderResponses, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of(
                    "message", "You are not allowed to see order details"
            ), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getByOrderId(@PathVariable("orderId") Integer orderId) {
        OrderResponse orderResponse = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @GetMapping("/order-lines/{orderId}")
    public ResponseEntity<?> findByOrderId(@PathVariable("orderId") Integer orderId) {
        List<OrderLineResponse> orderLineResponses = orderLineService.findAllByOrderId(orderId);
        return new ResponseEntity<>(orderLineResponses, HttpStatus.OK);
    }
}