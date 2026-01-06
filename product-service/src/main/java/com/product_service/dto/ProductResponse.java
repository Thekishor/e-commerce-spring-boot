package com.product_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Integer productId;
    private String name;
    private String description;
    private Integer quantity;
    private Long price;
    private String categoryName;
}
