package com.project.flow.credential.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Response record returning safe credential metadata to users.
 * Secrets themselves are masked and only their keys are exposed.
 *
 * @param id credential ID
 * @param name name description
 * @param provider integration provider name
 * @param secretKeys list of secret keys stored (excluding values)
 * @param createdAt creation timestamp
 * @param updatedAt update timestamp
 */
public record CredentialResponse(
    String id,
    String name,
    String provider,
    Set<String> secretKeys,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
