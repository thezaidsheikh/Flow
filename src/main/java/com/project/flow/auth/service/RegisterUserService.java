package com.project.flow.auth.service;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.dto.request.RegisterUserReqDto;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterUserReqDto userReq) throws UsernameNotFoundException {
        if (userRepository.existsByEmail(userReq.email().toLowerCase())) {
            throw new UsernameNotFoundException("User with email " + userReq.email() + " already exists");
        }

        User user = User.builder().firstName(userReq.firstName()).lastName(userReq.lastName()).email(userReq.email())
                        .password(passwordEncoder.encode(userReq.password())).status(UserStatus.ACTIVE).build();

        return userRepository.save(user);
    }
}