package common.events.kafka;

import lombok.*;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRegisterEvent {

    private String event;
    private String userId;
    private String email;
    private Map<String, Object> template;
}
