package com.project.flow.credential.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.common.exception.UnauthorizedException;
import com.project.flow.credential.domain.Credential;
import com.project.flow.credential.encryption.CredentialEncryptor;
import com.project.flow.credential.repository.CredentialRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to load and decrypt raw credentials for internal process execution.
 */
@Service
@RequiredArgsConstructor
public class GetCredentialService {

    private final CredentialRepository credentialRepository;
    private final CredentialEncryptor credentialEncryptor;

    /**
     * Retrieves the credential by ID, checks ownership, and decrypts the encrypted secrets.
     *
     * @param id credential ID
     * @param userId owner user ID
     * @return map of raw decrypted secrets
     */
    @Transactional(readOnly = true)
    public Map<String, String> getDecryptedSecrets(String id, String userId) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Credential not found with ID: " + id, null));

        if (!credential.getUserId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to credential", Optional.empty());
        }

        return credentialEncryptor.decrypt(credential.getEncryptedSecrets());
    }
}
