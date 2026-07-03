package com.project.flow.auth.service;

import com.project.flow.auth.domain.RefreshToken;
import com.project.flow.auth.domain.User;
import com.project.flow.auth.repository.RefreshTokenRepository;
import com.project.flow.auth.repository.UserRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void save(String userId, String token, Date expiryDate) {
        try {
            User user = userRepository.getReferenceById(userId);

            RefreshToken refreshToken = RefreshToken.builder().token(token).expiryDate(expiryDate).revoked(false).user(user).build();

            refreshTokenRepository.save(refreshToken);
        } catch (RuntimeException e) {
            log.error("Failed to persist refresh token for userId={}", userId, e);
        }
    }
}
