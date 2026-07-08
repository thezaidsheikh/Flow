package com.project.flow.connector.domain;

import java.util.Map;

/**
 * Record representing a specific integration action schema inside a connector.
 *
 * @param action unique action type (e.g. create_pull_request)
 * @param displayName user-friendly action name
 * @param description action purpose description
 * @param inputSchema schema defining field keys, types, required flags, and descriptions
 */
public record ConnectorActionDefinition(
    String action,
    String displayName,
    String description,
    Map<String, Object> inputSchema
) {}
