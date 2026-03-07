package common.events.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
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
