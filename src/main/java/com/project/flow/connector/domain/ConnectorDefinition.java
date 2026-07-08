package com.project.flow.connector.domain;

import java.util.List;
import java.util.Map;

/**
 * Record describing the metadata and action/configuration schema of an integration connector.
 *
 * @param provider unique provider name (e.g. github)
 * @param displayName user-friendly name of the connector
 * @param description short summary of connector capabilities
 * @param actions list of integration actions supported
 * @param credentialSchema schema defining required credential configuration fields
 */
public record ConnectorDefinition(
    String provider,
    String displayName,
    String description,
    List<ConnectorActionDefinition> actions,
    Map<String, String> credentialSchema
) {}
