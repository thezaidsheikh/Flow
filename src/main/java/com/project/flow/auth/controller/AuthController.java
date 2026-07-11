package com.project.flow.auth.controller;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.LoginReqDto;
import com.project.flow.auth.dto.request.RefreshTokenReqDto;
import com.project.flow.auth.dto.request.RegisterUserReqDto;
import com.project.flow.auth.dto.response.AuthResDto;
import com.project.flow.auth.dto.response.UserResDto;
import com.project.flow.auth.service.GetCurrentUserService;
import com.project.flow.auth.service.LoginUserService;
import com.project.flow.auth.service.RefreshAccessTokenService;
import com.project.flow.auth.service.RegisterUserService;
import com.project.flow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final RefreshAccessTokenService refreshAccessTokenService;
    private final GetCurrentUserService getCurrentUserService;

    @PostMapping("/register")
    public ApiResponse<UserResDto> registerUser(@Valid @RequestBody RegisterUserReqDto registerUserReqDto) {
        User user = registerUserService.register(registerUserReqDto);
        UserResDto response = new UserResDto(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getStatus().name());
        return ApiResponse.<UserResDto>builder().success(true).statusCode(201).message("User registered successfully").data(response).build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResDto> loginUser(@Valid @RequestBody LoginReqDto payload) {
        AuthResDto response = loginUserService.login(payload);
        return ApiResponse.<AuthResDto>builder().success(true).statusCode(200).message("User logged in successfully").data(response).build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResDto> refreshAccessToken(@Valid @RequestBody RefreshTokenReqDto payload) {
        AuthResDto response = refreshAccessTokenService.execute(payload);
        return ApiResponse.<AuthResDto>builder().success(true).statusCode(200).message("Access token refreshed successfully").data(response).build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResDto> getCurrentUser() {
        UserResDto response = getCurrentUserService.execute();
        return ApiResponse.<UserResDto>builder().success(true).statusCode(200).message("Current user retrieved successfully").data(response).build();
    }
}
