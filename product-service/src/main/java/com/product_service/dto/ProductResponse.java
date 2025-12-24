package com.product_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductResponse {

    private String name;
    private String description;
    private Integer quantity;
    private Long price;
    private String categoryName;
}
