package com.project.flow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Value("${spring.application.name:flow}")
    private String applicationName;

    @Value("${server.port:3002}")
    private String serverPort;

    @Bean
    public OpenAPI flowOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Development Server");

        Server prodServer = new Server()
                .url("https://api.flow.example.com" + contextPath)
                .description("Production Server");

        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION)
                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .servers(List.of(devServer, prodServer))
                .info(apiInfo())
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .security(List.of(securityRequirement))
                .tags(apiTags());
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName + " API")
                .version("v1.0.0")
                .description("""
                        # Flow Workflow Automation Platform API
                        
                        RESTful API for the Flow workflow automation platform. This API allows you to:
                        - **Authenticate** users and manage JWT tokens
                        - **Manage Workflows** - Create, draft, publish, and retrieve workflows with visual graph editing
                        - **Execute Workflows** - Run published workflows and track execution history
                        - **Manage Credentials** - Securely store and manage encrypted credentials for integrations
                        - **Integrate Connectors** - Discover and execute actions from supported connectors
                        
                        ## Authentication
                        All endpoints (except `/auth/register`, `/auth/login`, `/auth/refresh`, and actuator endpoints) 
                        require a valid JWT Bearer token in the Authorization header:
                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```
                        
                        ## Error Responses
                        All error responses follow a standard format:
                        ```json
                        {
                          "success": false,
                          "statusCode": 400,
                          "message": "Validation failed",
                          "error": {
                            "code": "VALIDATION_ERROR"
                          }
                        }
                        ```
                        
                        ## Pagination
                        List endpoints support pagination via `page` (0-based) and `size` query parameters.
                        """)
                .contact(new Contact()
                        .name("Flow Team")
                        .email("support@flow.example.com")
                        .url("https://github.com/flow"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://flow.example.com/license"));
    }

    private List<Tag> apiTags() {
        return List.of(
                new Tag().name("Authentication").description("User authentication and token management endpoints"),
                new Tag().name("Workflows").description("Workflow CRUD operations, draft management, and publishing"),
                new Tag().name("Workflow Executions").description("Run workflows, list executions, view details and logs"),
                new Tag().name("Credentials").description("Manage encrypted credentials for external integrations"),
                new Tag().name("Connectors").description("Discover available connectors and execute connector actions")
        );
    }
}