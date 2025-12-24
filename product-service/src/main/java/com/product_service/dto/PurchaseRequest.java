package com.product_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @NotNull(message = "Product Id can not be null")
    private Integer productId;

    @NotNull(message = "Product quantity can not be null")
    @Positive(message = "Product quantity should be greater than 0")
    private Integer quantity;
}
