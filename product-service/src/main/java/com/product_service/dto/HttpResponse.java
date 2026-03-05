package com.product_service.dto;

import lombok.*;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HttpResponse {

    private String message;
    private boolean status;
    private Instant instant;
}
