package com.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseRequest {

    @NotNull(message = "Product Id cannot be null")
    private Integer productId;

    @NotNull(message = "Product quantity cannot be null")
    @Positive(message = "Product quantity should be greater than 0")
    private Integer quantity;

}
