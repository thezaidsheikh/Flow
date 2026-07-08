package com.project.flow.credential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Request record for creating a new credential configuration.
 *
 * @param name name description of the credential (e.g. My GitHub Token)
 * @param provider name of the external integration provider (e.g. github)
 * @param secrets map of sensitive secret fields to be encrypted
 */
public record CreateCredentialRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Provider is required") String provider,
    @NotEmpty(message = "Secrets cannot be empty") Map<String, String> secrets
) {}
