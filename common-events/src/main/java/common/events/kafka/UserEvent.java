package common.events.kafka;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEvent {

    private String eventId;
    private String username;
    private UUID userId;
    private String email;
    private String url;
    private LocalDateTime localDateTime;
}
