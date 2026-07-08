package com.project.flow.credential.service;

import com.project.flow.credential.domain.Credential;
import com.project.flow.credential.dto.request.CreateCredentialRequest;
import com.project.flow.credential.dto.response.CredentialResponse;
import com.project.flow.credential.encryption.CredentialEncryptor;
import com.project.flow.credential.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to execute the creation of a user's credential.
 * Secrets are encrypted using symmetric AES-256 GCM before database persistence.
 */
@Service
@RequiredArgsConstructor
public class CreateCredentialService {

    private final CredentialRepository credentialRepository;
    private final CredentialEncryptor credentialEncryptor;

    /**
     * Encrypts and saves the credential request payload.
     *
     * @param request credential fields (name, provider, secrets map)
     * @param userId the ID of the authenticated user
     * @return response metadata
     */
    @Transactional
    public CredentialResponse execute(CreateCredentialRequest request, String userId) {
        String encrypted = credentialEncryptor.encrypt(request.secrets());

        Credential credential = Credential.builder()
                .userId(userId)
                .name(request.name())
                .provider(request.provider().toLowerCase())
                .encryptedSecrets(encrypted)
                .build();

        credential = credentialRepository.save(credential);

        return new CredentialResponse(
                credential.getId(),
                credential.getName(),
                credential.getProvider(),
                request.secrets().keySet(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }
}
