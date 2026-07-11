package com.project.flow.auth.service;

import com.project.flow.auth.dto.response.UserResDto;
import com.project.flow.common.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUserService {

    private final CurrentUserProvider currentUserProvider;

    public UserResDto execute() {
        var user = currentUserProvider.getCurrentUser();
        return new UserResDto(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getStatus().name());
    }
}
