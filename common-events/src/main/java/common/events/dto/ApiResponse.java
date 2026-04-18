package common.events.dto;

import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {

    private String message;
    private T data;
    private boolean success;
    private int status;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
