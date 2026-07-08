package com.project.flow.connector.service;

import com.project.flow.connector.domain.ConnectorActionDefinition;
import com.project.flow.connector.domain.ConnectorDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Service to retrieve schemas and descriptors of all integration connectors.
 */
@Service
public class ListConnectorsService {

    /**
     * Exposes a static registry configuration of supported connectors.
     * Can be refactored to read from database or config files.
     *
     * @return list of connector definitions
     */
    public List<ConnectorDefinition> execute() {
        List<ConnectorDefinition> connectors = new ArrayList<>();

        // GitHub Connector Schema
        Map<String, String> githubCredSchema = new HashMap<>();
        githubCredSchema.put("token", "GitHub Personal Access Token or OAuth Token");

        List<ConnectorActionDefinition> githubActions = new ArrayList<>();

        Map<String, Object> prInputSchema = new HashMap<>();
        prInputSchema.put("repository", Map.of("type", "string", "required", true, "description", "GitHub repository path in owner/repo format"));
        prInputSchema.put("title", Map.of("type", "string", "required", true, "description", "Title of the Pull Request"));
        prInputSchema.put("head", Map.of("type", "string", "required", true, "description", "The name of the branch where changes are implemented (source branch)"));
        prInputSchema.put("base", Map.of("type", "string", "required", true, "description", "The name of the branch you want changes pulled into (target branch)"));
        prInputSchema.put("body", Map.of("type", "string", "required", false, "description", "The pull request description details"));

        githubActions.add(new ConnectorActionDefinition(
                "create_pull_request",
                "Create Pull Request",
                "Creates a pull request on the specified GitHub repository",
                prInputSchema
        ));

        connectors.add(new ConnectorDefinition(
                "github",
                "GitHub",
                "Integrate GitHub to automate pull requests, issue tracking, and commits",
                githubActions,
                githubCredSchema
        ));

        return connectors;
    }
}
