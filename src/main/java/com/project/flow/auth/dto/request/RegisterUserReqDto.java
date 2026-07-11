package com.project.flow.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RegisterUserReqDto(
    @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

    @NotBlank(message = "Password is required") String password,

    @NotBlank(message = "First name is required") String firstName,

    @NotBlank(message = "Last name is required") String lastName
) {}
