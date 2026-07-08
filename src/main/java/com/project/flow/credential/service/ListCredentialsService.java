package com.project.flow.credential.service;

import com.project.flow.credential.domain.Credential;
import com.project.flow.credential.dto.response.CredentialResponse;
import com.project.flow.credential.encryption.CredentialEncryptor;
import com.project.flow.credential.repository.CredentialRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to retrieve a list of credentials belonging to a user, with masked secret details.
 */
@Service
@RequiredArgsConstructor
public class ListCredentialsService {

    private final CredentialRepository credentialRepository;
    private final CredentialEncryptor credentialEncryptor;

    /**
     * Lists credentials belonging to a user, decrypting secrets only to return their keys.
     *
     * @param userId user ID
     * @return list of credentials responses
     */
    @Transactional(readOnly = true)
    public List<CredentialResponse> execute(String userId) {
        List<Credential> credentials = credentialRepository.findAllByUserId(userId);
        return credentials.stream().map(this::toResponse).toList();
    }

    private CredentialResponse toResponse(Credential credential) {
        Map<String, String> decrypted = credentialEncryptor.decrypt(credential.getEncryptedSecrets());
        return new CredentialResponse(
                credential.getId(),
                credential.getName(),
                credential.getProvider(),
                decrypted.keySet(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }
}
