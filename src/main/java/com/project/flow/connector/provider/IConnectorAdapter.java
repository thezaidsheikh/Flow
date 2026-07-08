package com.project.flow.connector.provider;

import java.util.Map;

/**
 * Base interface for all integration connector adapters.
 * Implementations use the Adapter Pattern to perform actions on external APIs.
 */
public interface IConnectorAdapter {

    /**
     * Retrieves the identifier of the integration provider.
     *
     * @return unique provider identifier (e.g. github)
     */
    String getProvider();

    /**
     * Executes a specific integration action using inputs and credentials secrets.
     *
     * @param action the action type (e.g. create_pull_request)
     * @param inputs execution variables provided by the user/node
     * @param decryptedSecrets raw decrypted credentials secrets (e.g. API tokens)
     * @return map containing action output payload
     */
    Map<String, Object> execute(String action, Map<String, Object> inputs, Map<String, String> decryptedSecrets);
}
