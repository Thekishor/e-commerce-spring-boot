package com.order_service.service;

import com.order_service.dto.OrderRequest;
import com.order_service.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, String authHeader, String userId);

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Integer orderId);
}
