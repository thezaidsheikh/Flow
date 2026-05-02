package com.project.flow.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class RegisterUserDTO {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;

    @NotBlank
    public String firstName;

    @NotBlank
    public String lastName;
}