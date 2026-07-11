package com.project.flow.connector.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ExecuteConnectorActionRequest(
    @NotBlank(message = "Credential id is required") String credentialId,
    @NotNull(message = "Inputs are required") Map<String, Object> inputs
) {}
