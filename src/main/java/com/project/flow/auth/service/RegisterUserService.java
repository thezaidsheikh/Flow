package com.project.flow.auth.service;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.RegisterUserReqDto;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.enums.UserRole;
import com.project.flow.common.enums.UserStatus;
import com.project.flow.common.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterUserReqDto userReq) {
        if (userRepository.existsByEmail(userReq.email().toLowerCase())) {
            throw new ResourceConflictException("User with email " + userReq.email() + " already exists", null);
        }

        User user = User.builder()
            .firstName(userReq.firstName())
            .lastName(userReq.lastName())
            .email(userReq.email())
            .password(passwordEncoder.encode(userReq.password()))
            .status(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .build();

        return userRepository.save(user);
    }
}
