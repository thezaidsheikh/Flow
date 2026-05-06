package com.project.flow.auth.dto.response;

import java.util.UUID;

public record UserResDto(
    UUID id,
    String name,
    String email,
    String status
) {}
