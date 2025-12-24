package com.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name can not be null")
    private String name;

    @NotBlank(message = "Category description can not be null")
    @Size(min = 4, max = 100, message = "Category description must be between 4 and 100 characters")
    private String description;
}
