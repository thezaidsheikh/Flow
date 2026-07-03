package com.project.flow.auth.service;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.LoginReqDto;
import com.project.flow.auth.dto.response.AuthResDto;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.exception.InvalidCredentialsException;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.common.security.JwtService;
import com.project.flow.common.security.TokenGenerator;
import com.project.flow.config.JwtConfig;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginUserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final SaveRefreshTokenService saveRefreshTokenService;

    // Service for user login
    public AuthResDto login(LoginReqDto payload) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(payload.email(), payload.password()));
            if (!authentication.isAuthenticated()) {
                throw new InvalidCredentialsException("Invalid credentials", Optional.empty());
            }

            User user = userRepository.findByEmail(payload.email().toLowerCase()).orElseThrow(() -> new ResourceNotFound("User not found", Optional.empty()));

            String accessToken = jwtService.generateAccessToken(UUID.fromString(user.getId()), user.getEmail(), user.getStatus());
            String refreshToken = tokenGenerator.generateToken();
            String tokenType = "Bearer";
            long expiresIn = jwtConfig.getExpiration() / 1000;

            saveRefreshTokenService.save(user.getId(), refreshToken, new Date(System.currentTimeMillis() + jwtConfig.getExpiration()));

            return new AuthResDto(accessToken, refreshToken, tokenType, expiresIn);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password", e, Optional.empty());
        }
    }
}
