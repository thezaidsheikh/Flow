# Flow

Flow is a backend-first workflow automation platform built as a modular Spring Boot application. It currently supports user authentication, workflow drafting and publishing, encrypted credential storage, connector metadata, manual and webhook-triggered workflow execution, and run history with node-level logs.

## Implemented MVP

- JWT authentication with register, login, refresh-token rotation, and current-user lookup
- Workflow CRUD for authenticated users
- Draft graph persistence with nodes and edges
- Workflow publish flow with graph validation
- Manual execution of published workflows
- Webhook-triggered execution of published workflows (HMAC-SHA256 signature validation)
- Run history and node execution logs
- Encrypted credential storage
- Connector catalog plus executable GitHub pull-request action

## Current runtime support

Supported workflow node types:

- `TRIGGER`: starts a run (manual or webhook-triggered)
- `CONDITION`: evaluates a field from `trigger`, `variables`, or `previous`
- `ACTION`: executes a connector action with resolved inputs
- `DELAY`: storable in drafts, but not executable yet in the current synchronous runtime

Currently implemented connector actions:

- `github.create_pull_request`

## Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Spring Modulith
- PostgreSQL
- Gradle

## Project structure

```text
src/main/java/com/project/flow
├── auth
├── common
├── config
├── connector
├── credential
├── execution
├── run
└── workflow
```

## Prerequisites

- Java 21
- Docker with Compose support

## Local setup

1. Copy the environment template.

```bash
cp .env.example .env
```

2. Start PostgreSQL.

```bash
docker compose up -d
```

3. Start the application.

```bash
./gradlew bootRun
```

If your local Gradle home is blocked or not writable, use:

```bash
GRADLE_USER_HOME="$PWD/.gradle-local" ./gradlew bootRun
```

The API starts on `http://localhost:3002/api/v1`.

## Environment variables

`APP_PORT`
- HTTP port for the API

`DB_URL`
- Full JDBC URL for PostgreSQL

`DB_USERNAME`
- Database username

`DB_PASSWORD`
- Database password

`JWT_SECRET`
- Secret used to sign access tokens

`JWT_EXPIRATION`
- Access-token lifetime in milliseconds

`ENCRYPTION_KEY`
- Optional override for credential encryption; if absent, `JWT_SECRET` is used as the source key material

## Build and test

Run tests:

```bash
./gradlew test
```

Create the application jar:

```bash
./gradlew build
```

## API surface

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /auth/me`

### Workflows

- `POST /workflows`
- `GET /workflows`
- `GET /workflows/{id}`
- `PUT /workflows/{id}/draft`
- `POST /workflows/{id}/publish`
- `POST /workflows/{id}/run`

### Runs

- `GET /workflows/{id}/runs`
- `GET /runs/{runId}`
- `GET /runs/{runId}/logs`

### Webhooks (No Auth Required)

- `POST /hooks/{path}`

### Credentials

- `POST /credentials`
- `GET /credentials`
- `DELETE /credentials/{id}`

### Connectors

- `GET /connectors`
- `GET /connectors/{provider}`
- `POST /connectors/{provider}/actions/{action}/execute`

## Workflow graph payload

Draft save request:

```json
{
  "nodes": [
    {
      "id": "trigger-node",
      "name": "Manual Trigger",
      "type": "TRIGGER",
      "position_x": 100,
      "position_y": 80,
      "config": {}
    },
    {
      "id": "condition-node",
      "name": "Check Repository",
      "type": "CONDITION",
      "position_x": 320,
      "position_y": 80,
      "config": {
        "source": "trigger",
        "field": "repository",
        "operator": "equals",
        "value": "octocat/Hello-World"
      }
    }
  ],
  "edges": [
    {
      "id": "edge-1",
      "source_node_id": "trigger-node",
      "target_node_id": "condition-node"
    }
  ]
}
```

Supported condition operators:

- `equals`
- `not_equals`
- `exists`

## Action node config

GitHub pull-request action example:

```json
{
  "provider": "github",
  "action": "create_pull_request",
  "credentialId": "credential-id",
  "inputs": {
    "repository": "${trigger.repository}",
    "title": "Create PR for ${previous.branch}",
    "head": "${previous.branch}",
    "base": "${variables.baseBranch}",
    "body": "Opened by Flow"
  }
}
```

Available placeholder roots:

- `${trigger.<field>}`
- `${variables.<field>}`
- `${previous.<field>}`

## Run behavior

- Runs execute synchronously in the current application process.
- Each executed node writes a log entry.
- Branching is only supported on `CONDITION` nodes.
- Delay nodes are rejected during execution with a clear validation error.

## Current limitations

- No scheduler, queue worker, or background execution yet
- No Slack, email, or HTTP action nodes yet
- No refresh-token revocation endpoint
- No Flyway/Liquibase migrations yet; schema is managed by Hibernate in the current MVP

## Documentation

- [README.md](/Users/thezaidsheikh/Zaid%20-%20F1/Sheikh's%20Personal%20Projects/flow/README.md)
- [ARCHITECTURE.md](/Users/thezaidsheikh/Zaid%20-%20F1/Sheikh's%20Personal%20Projects/flow/ARCHITECTURE.md)
- [HELP.md](/Users/thezaidsheikh/Zaid%20-%20F1/Sheikh's%20Personal%20Projects/flow/HELP.md)
