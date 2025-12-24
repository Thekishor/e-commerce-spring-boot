package com.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name can not be blank")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Product description can not be blank")
    @Size(min = 5, max = 100, message = "Product description must be between 5 and 100 characters")
    private String description;

    @Positive(message = "Product quantity greater than 0")
    private Integer quantity;

    @Positive(message = "Product price contains positive")
    private Long price;

    @NotNull(message = "Category Id cannot be null")
    @Positive(message = "Category Id should be positive")
    private Integer categoryId;
}
