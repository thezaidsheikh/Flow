package com.project.flow.auth.dto.response;

public record AuthResDto(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {}
