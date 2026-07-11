package com.project.flow.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.flow.auth.domain.RefreshToken;
import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.RefreshTokenReqDto;
import com.project.flow.auth.repository.RefreshTokenRepository;
import com.project.flow.common.enums.UserRole;
import com.project.flow.common.enums.UserStatus;
import com.project.flow.common.exception.TokenExpiredException;
import com.project.flow.common.security.JwtService;
import com.project.flow.common.security.TokenGenerator;
import com.project.flow.config.JwtConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefreshAccessTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;
    private TokenGenerator tokenGenerator;
    private JwtConfig jwtConfig;
    private SaveRefreshTokenService saveRefreshTokenService;
    private RefreshAccessTokenService refreshAccessTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtService = mock(JwtService.class);
        tokenGenerator = mock(TokenGenerator.class);
        saveRefreshTokenService = mock(SaveRefreshTokenService.class);
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-that-is-long-enough-for-jwt-signing");
        jwtConfig.setExpiration(Duration.ofHours(1).toMillis());

        refreshAccessTokenService = new RefreshAccessTokenService(
            refreshTokenRepository,
            jwtService,
            tokenGenerator,
            jwtConfig,
            saveRefreshTokenService
        );
    }

    @Test
    void shouldRotateRefreshTokenAndReturnNewTokens() {
        User user = User.builder()
            .id(UUID.randomUUID().toString())
            .email("user@example.com")
            .password("encoded")
            .firstName("Flow")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .build();

        RefreshToken refreshToken = RefreshToken.builder()
            .token("old-refresh")
            .expiryDate(Date.from(Instant.now().plus(Duration.ofHours(1))))
            .revoked(false)
            .user(user)
            .build();

        when(refreshTokenRepository.findByToken("old-refresh")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(any(UUID.class), eq("user@example.com"), eq(UserStatus.ACTIVE))).thenReturn("new-access");
        when(tokenGenerator.generateToken()).thenReturn("new-refresh");

        var response = refreshAccessTokenService.execute(new RefreshTokenReqDto("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        verify(refreshTokenRepository).delete(refreshToken);
        verify(saveRefreshTokenService).save(eq(user.getId()), eq("new-refresh"), any(Date.class));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        User user = User.builder()
            .id(UUID.randomUUID().toString())
            .email("user@example.com")
            .password("encoded")
            .firstName("Flow")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .build();

        RefreshToken refreshToken = RefreshToken.builder()
            .token("expired-refresh")
            .expiryDate(Date.from(Instant.now().minus(Duration.ofMinutes(1))))
            .revoked(false)
            .user(user)
            .build();

        when(refreshTokenRepository.findByToken("expired-refresh")).thenReturn(Optional.of(refreshToken));

        assertThrows(TokenExpiredException.class, () -> refreshAccessTokenService.execute(new RefreshTokenReqDto("expired-refresh")));
    }
}
