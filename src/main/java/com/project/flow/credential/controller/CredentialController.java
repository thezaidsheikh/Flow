package com.project.flow.credential.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.credential.dto.request.CreateCredentialRequest;
import com.project.flow.credential.dto.response.CredentialResponse;
import com.project.flow.credential.service.CreateCredentialService;
import com.project.flow.credential.service.DeleteCredentialService;
import com.project.flow.credential.service.ListCredentialsService;
import com.project.flow.common.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing REST API endpoints for user credential management.
 */
@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final CreateCredentialService createCredentialService;
    private final ListCredentialsService listCredentialsService;
    private final DeleteCredentialService deleteCredentialService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ApiResponse<CredentialResponse> createCredential(@Valid @RequestBody CreateCredentialRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        CredentialResponse response = createCredentialService.execute(request, userId);
        return ApiResponse.<CredentialResponse>builder()
                .success(true)
                .statusCode(200)
                .message("Credential created successfully")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<CredentialResponse>> getCredentials() {
        String userId = currentUserProvider.getCurrentUserId();
        List<CredentialResponse> response = listCredentialsService.execute(userId);
        return ApiResponse.<List<CredentialResponse>>builder()
                .success(true)
                .statusCode(200)
                .message("Credentials retrieved successfully")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCredential(@PathVariable String id) {
        String userId = currentUserProvider.getCurrentUserId();
        deleteCredentialService.execute(id, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .statusCode(200)
                .message("Credential deleted successfully")
                .build();
    }
}
