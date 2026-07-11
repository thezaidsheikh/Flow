package com.project.flow.common.security;

import com.project.flow.auth.domain.User;
import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private static final String ANONYMOUS_PRINCIPAL = "anonymousUser";

    private final UserRepository userRepository;

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required", null);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required", null);
        }

        String userId = principal.toString();
        if (userId.isBlank() || ANONYMOUS_PRINCIPAL.equalsIgnoreCase(userId)) {
            throw new UnauthorizedException("Authentication is required", null);
        }

        return userId;
    }

    public User getCurrentUser() {
        String userId = getCurrentUserId();
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFound("User not found", null));
    }
}
