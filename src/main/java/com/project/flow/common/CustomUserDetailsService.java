package com.project.flow.common;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.enums.UserStatus;
import com.project.flow.common.exception.ResourceNotFound;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws ResourceNotFound {
        return userRepository
            .findByEmail(email.toLowerCase())
            .map(this::mapUserToUserDetails)
            .orElseThrow(() -> new ResourceNotFound("User not found with email: " + email, null));
    }

    private UserDetails mapUserToUserDetails(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getStatus() == UserStatus.ACTIVE,
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }
}
