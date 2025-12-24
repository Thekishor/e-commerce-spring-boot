package com.order_service.service;

import com.order_service.dto.OrderLineRequest;
import com.order_service.dto.OrderLineResponse;
import com.order_service.entities.Order;
import com.order_service.entities.OrderLine;
import com.order_service.exception.ResourceNotFoundException;
import com.order_service.repository.OrderLineRepository;
import com.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;

    @Override
    public void createOrderLine(OrderLineRequest orderLineRequest) {
        Order order = orderRepository.findById(orderLineRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id" + orderLineRequest.getOrderId()));
        System.out.println(order);
        log.info("Order Information: {}", order);
        OrderLine orderLine = mapOrderLineRequestToOrderLineEntity(orderLineRequest);
        orderLine.setOrder(order);
        orderLineRepository.save(orderLine);
    }

    private OrderLine mapOrderLineRequestToOrderLineEntity(OrderLineRequest orderLineRequest) {

        return OrderLine.builder()
                .productId(orderLineRequest.getProductId())
                .quantity(orderLineRequest.getQuantity())
                .build();
    }

    @Override
    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        List<OrderLine> orderLines = orderLineRepository.findAllByOrderId(orderId);
        return orderLines.stream().map(this::mapOrderLineEntityToOrderLineResponse).toList();
    }

    private OrderLineResponse mapOrderLineEntityToOrderLineResponse(OrderLine orderLine) {
        return OrderLineResponse.builder()
                .id(orderLine.getId())
                .quantity(orderLine.getQuantity())
                .build();
    }
}