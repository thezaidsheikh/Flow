package com.project.flow.credential.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.common.exception.UnauthorizedException;
import com.project.flow.credential.domain.Credential;
import com.project.flow.credential.repository.CredentialRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to delete a credential and ensure proper ownership verification.
 */
@Service
@RequiredArgsConstructor
public class DeleteCredentialService {

    private final CredentialRepository credentialRepository;

    /**
     * Deletes the credential with the specified ID if owned by the current user.
     *
     * @param id credential ID
     * @param userId user ID
     */
    @Transactional
    public void execute(String id, String userId) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Credential not found with ID: " + id, null));

        if (!credential.getUserId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to credential", Optional.empty());
        }

        credentialRepository.delete(credential);
    }
}
