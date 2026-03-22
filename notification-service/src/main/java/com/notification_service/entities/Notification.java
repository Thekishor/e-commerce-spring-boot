package com.notification_service.entities;

import com.notification_service.constant.NotificationEvent;
import com.notification_service.constant.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_db")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private NotificationEvent notificationEvent;

    private LocalDateTime localDateTime;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_id", unique = true, nullable = false, updatable = false)
    private String userId;

    @Column(name = "event_id", unique = true, nullable = false, updatable = false)
    private String eventId;

    private NotificationStatus notificationStatus;
}
