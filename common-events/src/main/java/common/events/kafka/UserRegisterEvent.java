package common.events.kafka;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRegisterEvent {

    private String eventId;
    private String username;
    private String userId;
    private String email;
    private String url;
    private LocalDateTime localDateTime;
}
