package com.project.flow.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenReqDto(@NotBlank(message = "Refresh token is required") String refreshToken) {}
