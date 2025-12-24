package com.product_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PurchaseResponse {

    private Integer productId;
    private String name;
    private String description;
    private Long price;
    private Integer quantity;
}
