package com.project.flow.auth.service;

import com.project.flow.auth.domain.RefreshToken;
import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.RefreshTokenReqDto;
import com.project.flow.auth.dto.response.AuthResDto;
import com.project.flow.auth.repository.RefreshTokenRepository;
import com.project.flow.common.exception.TokenExpiredException;
import com.project.flow.common.exception.UnauthorizedException;
import com.project.flow.common.security.JwtService;
import com.project.flow.common.security.TokenGenerator;
import com.project.flow.config.JwtConfig;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshAccessTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final JwtConfig jwtConfig;
    private final SaveRefreshTokenService saveRefreshTokenService;

    @Transactional
    public AuthResDto execute(RefreshTokenReqDto request) {
        RefreshToken storedRefreshToken = refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid", Optional.empty()));

        if (storedRefreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token is revoked", Optional.empty());
        }

        if (storedRefreshToken.getExpiryDate().toInstant().isBefore(Instant.now())) {
            throw new TokenExpiredException("Refresh token expired", Optional.empty());
        }

        User user = storedRefreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(UUID.fromString(user.getId()), user.getEmail(), user.getStatus());
        String rotatedRefreshToken = tokenGenerator.generateToken();

        refreshTokenRepository.delete(storedRefreshToken);
        saveRefreshTokenService.save(user.getId(), rotatedRefreshToken, new Date(System.currentTimeMillis() + jwtConfig.getExpiration()));

        return new AuthResDto(accessToken, rotatedRefreshToken, "Bearer", jwtConfig.getExpiration() / 1000);
    }
}
