package com.project.flow.auth.controller;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.LoginReqDto;
import com.project.flow.auth.dto.request.RegisterUserReqDto;
import com.project.flow.auth.dto.response.AuthResDto;
import com.project.flow.auth.dto.response.UserResDto;
import com.project.flow.auth.service.LoginUserService;
import com.project.flow.auth.service.RegisterUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    // Controller for user registration
    @PostMapping("/register")
    public UserResDto registerUser(@Valid @RequestBody RegisterUserReqDto registerUserReqDto) {
        User user = registerUserService.register(registerUserReqDto);
        return new UserResDto(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getStatus().name());
    }

    // Controller for user login
    @PostMapping("/login")
    public AuthResDto loginUser(@Valid @RequestBody LoginReqDto payload) {
        return loginUserService.login(payload);
    }
}
