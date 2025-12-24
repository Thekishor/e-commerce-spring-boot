package com.user_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verify_token_db")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "active_token")
    private String activationToken;

    @Column(name = "token_expiry")
    private LocalDateTime activationTokenExpiry;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "USER_VERIFY_TOKEN"))
    private User user;
}
