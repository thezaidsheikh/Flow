package com.project.flow.credential.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.credential.dto.request.CreateCredentialRequest;
import com.project.flow.credential.dto.response.CredentialResponse;
import com.project.flow.credential.service.CreateCredentialService;
import com.project.flow.credential.service.DeleteCredentialService;
import com.project.flow.credential.service.ListCredentialsService;
import com.project.flow.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Manage encrypted credentials for external integrations")
public class CredentialController {

    private final CreateCredentialService createCredentialService;
    private final ListCredentialsService listCredentialsService;
    private final DeleteCredentialService deleteCredentialService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @Operation(summary = "Create credential", description = "Store a new credential with encrypted secrets. Secret values are AES-256-GCM encrypted at rest and never returned in responses.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Credential created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
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
    @Operation(summary = "List all credentials", description = "Get all credentials for the authenticated user. Only secret key names are returned, never the actual values.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Credentials retrieved successfully")
    })
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
    @Operation(summary = "Delete credential", description = "Permanently delete a credential and its encrypted secrets.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Credential deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Credential not found")
    })
    public ApiResponse<Void> deleteCredential(
        @Parameter(description = "Credential ID") @PathVariable String id
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        deleteCredentialService.execute(id, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .statusCode(200)
                .message("Credential deleted successfully")
                .build();
    }
}
