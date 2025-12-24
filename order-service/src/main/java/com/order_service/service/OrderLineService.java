package com.order_service.service;

import com.order_service.dto.OrderLineRequest;
import com.order_service.dto.OrderLineResponse;

import java.util.List;

public interface OrderLineService {

    void createOrderLine(OrderLineRequest orderLineRequest);

    List<OrderLineResponse> findAllByOrderId(Integer orderId);
}
