package com.order_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderLineResponse {

    private Integer id;
    private Integer quantity;
}
