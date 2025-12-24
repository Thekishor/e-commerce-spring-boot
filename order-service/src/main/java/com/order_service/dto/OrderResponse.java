package com.order_service.dto;

import com.order_service.constant.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponse {

    private String orderNumber;
    private String reference;
    private Long amount;
    private PaymentMethod paymentMethod;
    private String userId;
}
