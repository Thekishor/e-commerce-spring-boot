package common.events.kafka;

import common.events.dto.PurchaseResponse;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderEvent {

    private String eventId;
    private String reference;
    private String orderNumber;
    private Long amount;
    private String paymentMethod;
    private String username;
    private String email;
    private String userId;
    private List<PurchaseResponse> purchaseResponseList;
}
