package com.project.flow.auth.domain;

import com.project.flow.common.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "refresh_tokens", indexes = { @Index(name = "idx_refresh_token_token", columnList = "token", unique = true), @Index(name = "idx_refresh_token_user", columnList = "user_id") })
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;
}
