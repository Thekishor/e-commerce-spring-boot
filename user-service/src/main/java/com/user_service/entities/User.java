package com.user_service.entities;

import com.user_service.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_db")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
public class User extends BaseEntity {

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private List<String> role;

    private Boolean isActive;

    private Boolean emailVerified;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void setUserDetails() {
        if (this.role == null) {
            this.role = List.of("USER");
        }
        if (this.isActive == null) {
            this.isActive = false;
        }
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
    }
}
