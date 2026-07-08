package com.project.flow.connector.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.connector.provider.IConnectorAdapter;
import com.project.flow.connector.registry.ConnectorRegistry;
import com.project.flow.credential.service.GetCredentialService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service facilitating the execution of connector actions.
 * Loads the user's credential, decrypts secrets, resolves the adapter, and performs invocation.
 */
@Service
@RequiredArgsConstructor
public class ExecuteConnectorActionService {

    private final ConnectorRegistry connectorRegistry;
    private final GetCredentialService getCredentialService;

    /**
     * Executes the integration action.
     *
     * @param userId authenticated user ID
     * @param credentialId ID of the credential configuration to decrypt and use
     * @param provider provider identifier (e.g. github)
     * @param action action identifier (e.g. create_pull_request)
     * @param inputs map of input properties
     * @return execution output payload
     */
    public Map<String, Object> execute(
            String userId,
            String credentialId,
            String provider,
            String action,
            Map<String, Object> inputs) {

        // 1. Fetch decrypted credentials secrets
        Map<String, String> decryptedSecrets = getCredentialService.getDecryptedSecrets(credentialId, userId);

        // 2. Resolve adapter
        IConnectorAdapter adapter = connectorRegistry.getAdapter(provider)
                .orElseThrow(() -> new ResourceNotFound("Connector provider not supported: " + provider, null));

        // 3. Execute the connector adapter
        return adapter.execute(action, inputs, decryptedSecrets);
    }
}
