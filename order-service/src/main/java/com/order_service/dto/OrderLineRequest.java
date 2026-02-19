package com.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineRequest {

    @NotBlank(message = "Order ID Required")
    private Integer orderId;

    @NotBlank(message = "Product Id Required")
    private Integer productId;

    @NotBlank(message = "Quantity required")
    @Positive(message = "Quantity should be greater than 0")
    private Integer quantity;
}
