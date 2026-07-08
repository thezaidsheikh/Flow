package com.project.flow.credential.domain;

import com.project.flow.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity representing an external integration credential (API token, client secret, etc.)
 * belonging to a user. Secrets are stored encrypted at rest.
 */
@Entity
@Table(name = "credentials", indexes = {
    @Index(name = "idx_credentials_user_id", columnList = "user_id"),
    @Index(name = "idx_credentials_provider", columnList = "provider")
})
@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Credential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "encrypted_secrets", nullable = false, columnDefinition = "text")
    private String encryptedSecrets;
}
