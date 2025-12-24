package com.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineRequest {

    @NotNull(message = "Order ID Required")
    private Integer orderId;

    @NotNull(message = "Product Id Required")
    private Integer productId;

    @Positive(message = "Quantity should be greater than 0")
    private Integer quantity;
}
