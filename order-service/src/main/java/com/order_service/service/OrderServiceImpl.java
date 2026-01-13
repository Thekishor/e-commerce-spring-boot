package com.order_service.service;

import com.order_service.dto.*;

import com.order_service.entities.Order;
import com.order_service.exception.ResourceNotFoundException;
import com.order_service.openFeign.ProductClient;
import com.order_service.openFeign.UserClient;
import com.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import common.events.kafkaEvents.OrderEvent;
import common.events.kafkaEvents.PurchaseResponse;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest, String authHeader, String userId) {

        UserResponse userResponse = userClient.findByUserId(userId, authHeader);
        if (userResponse == null) {
            log.error("User not found with userId: {}", userId);
            throw new ResourceNotFoundException("User not found with userId" + userId);
        }

        List<PurchaseResponse> purchaseResponses =
                productClient.purchaseResponses(orderRequest.getPurchaseRequest(), authHeader);

        Order order = mapOrderRequestToOrderEntity(orderRequest);
        order.setUserId(userId);
        order.setAmount(purchaseResponses.stream().mapToLong(PurchaseResponse::getPrice).sum());
        Order savedOrder = orderRepository.save(order);

        //for order line service
        for (PurchaseRequest purchaseRequest : orderRequest.getPurchaseRequest()) {
            orderLineService.createOrderLine(
                    OrderLineRequest.builder()
                            .orderId(savedOrder.getId())
                            .productId(purchaseRequest.getProductId())
                            .quantity(purchaseRequest.getQuantity())
                            .build()
            );
        }

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setReference(savedOrder.getReference());
        orderEvent.setOrderNumber(savedOrder.getOrderNumber());
        orderEvent.setPaymentMethod(savedOrder.getPaymentMethod().toString());
        orderEvent.setAmount(savedOrder.getAmount());
        orderEvent.setUsername(userResponse.getUsername());
        orderEvent.setEmail(userResponse.getEmail());
        orderEvent.setPurchaseResponseList(purchaseResponses);

        //creating order event for notification
        kafkaMessageProducer.sendOrderEventMessage(orderEvent);

        return mapOrderEntityToOrderResponse(savedOrder);
    }

    private OrderResponse mapOrderEntityToOrderResponse(Order savedOrder) {
        return OrderResponse.builder()
                .orderNumber(savedOrder.getOrderNumber())
                .reference(savedOrder.getReference())
                .amount(savedOrder.getAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .username(savedOrder.getUserId())
                .build();
    }

    private Order mapOrderRequestToOrderEntity(OrderRequest orderRequest) {
        final String orderNumber = generateOrderNumber();
        return Order.builder()
                .orderNumber(orderNumber)
                .reference(orderRequest.getReference())
                .paymentMethod(orderRequest.getPaymentMethod())
                .build();
    }

    private String generateOrderNumber() {
        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        //Prefix + date part
        String orderPrefix = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            randomPart.append(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
        }
        return orderPrefix + "-" + randomPart.toString();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapOrderEntityToOrderResponse).toList();
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapOrderEntityToOrderResponse(order);
    }
}
