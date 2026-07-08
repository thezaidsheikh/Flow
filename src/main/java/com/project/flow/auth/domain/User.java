package com.project.flow.auth.domain;

import com.project.flow.common.domain.BaseEntity;
import com.project.flow.common.enums.UserRole;
import com.project.flow.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", indexes = { @Index(name = "idx_users_email", columnList = "email", unique = true) })
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @PrePersist
    public void prePersist() {
        this.email = this.email.toLowerCase();
    }
}
