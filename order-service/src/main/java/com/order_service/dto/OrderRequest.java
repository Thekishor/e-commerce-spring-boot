package com.order_service.dto;

import com.order_service.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Reference cannot be empty")
    private String reference;

    @Positive(message = "Order total amount should be positive")
    private Long amount;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Purchase requests cannot be null")
    @NotEmpty(message = "You should at least purchase one product")
    private List<@Valid PurchaseRequest> purchaseRequest;
}
