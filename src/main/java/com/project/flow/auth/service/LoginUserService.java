package com.project.flow.auth.service;

import com.project.flow.auth.dto.request.LoginReqDto;
import com.project.flow.auth.dto.response.AuthResDto;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.exception.InvalidCredentialsException;
import javax.naming.AuthenticationException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginUserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    public AuthResDto login(LoginReqDto payload) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(payload.email(), payload.password()));
        if (!authentication.isAuthenticated()) {
            System.out.println("Testing");
            return null;
        }
        return null;
    }
}
