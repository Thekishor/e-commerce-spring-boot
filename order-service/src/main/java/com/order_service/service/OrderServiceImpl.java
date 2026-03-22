package com.order_service.service;

import com.order_service.dto.*;
import com.order_service.entities.Order;
import com.order_service.exception.BusinessException;
import com.order_service.exception.ErrorCode;
import com.order_service.feign.ProductClient;
import com.order_service.feign.UserClient;
import com.order_service.interceptor.UserContext;
import com.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import common.events.kafka.OrderEvent;
import common.events.dto.PurchaseResponse;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Value("${alphanumeric.value}")
    private String alphanumeric;

    private final SecureRandom random = new SecureRandom();

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Override
    public void createOrder(OrderRequest orderRequest, String authHeader) {

        final String userId = UserContext.getUserId();

        UserResponse userResponse = userClient.findByUserId(userId, authHeader);
        if (userResponse == null) {
            log.error("User not found with userId: {}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, userId);
        }

        List<PurchaseResponse> purchaseResponses =
                productClient.purchaseResponses(
                        orderRequest.getPurchaseRequest(),
                        authHeader,
                        UserContext.getUserId(),
                        UserContext.getUserEmail(),
                        UserContext.getUserRole()
                );

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

        //creating order event for notification
        kafkaMessageProducer.sendOrderEventMessage(OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .reference(savedOrder.getReference())
                .orderNumber(savedOrder.getOrderNumber())
                .paymentMethod(savedOrder.getPaymentMethod().toString())
                .amount(savedOrder.getAmount())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .userId(userResponse.getUserId())
                .purchaseResponseList(purchaseResponses)
                .build());
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
        //Prefix + date part
        String orderPrefix = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            randomPart.append(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
        }
        return orderPrefix + "-" + randomPart;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapOrderEntityToOrderResponse).toList();
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, orderId));
        return mapOrderEntityToOrderResponse(order);
    }
}
