# ARCHITECTURE.md

## Purpose
This document is the technical source of truth for Flow. It explains the system design, domain boundaries, execution model, schema strategy, scaling path, and tradeoffs.

Project: **Flow** - Workflow Automation Platform.

---

# 1. System Overview

Flow is a backend-first workflow automation platform where users build workflows using triggers, logic nodes, delays, and actions.

Example:
```text
Webhook Trigger -> Condition -> Delay -> Send Email
```

Current goal:
- Production-grade MVP
- Clean modular monolith
- Async workflow execution
- Deployable in 20-day capstone timeline
- Future SaaS expansion ready

---

# 2. Domain Model

## Core Entities

### User
Owns workflows and credentials.

### Workflow
Logical automation container visible to user.

### WorkflowVersion
Immutable snapshots.
States:
- draft
- published
- archived

### Node
A step in workflow graph.
Examples:
- webhook
- cron
- condition
- delay
- email
- slack
- http_request

### Edge
Directed connection between nodes.
Supports branching.

### Credential
Encrypted secrets / tokens.
Examples:
- Slack token
- SMTP config
- API key

### ConnectorDefinition
Metadata for integrations.
Defines schema and capability.

### WorkflowRun
Single execution instance.

### NodeRunLog
Per-node execution trace.

---

## Cardinality
```text
User 1 -> many Workflows
User 1 -> many Credentials
Workflow 1 -> many WorkflowVersions
Workflow 1 -> many WorkflowRuns
WorkflowVersion 1 -> many Nodes
WorkflowVersion 1 -> many Edges
WorkflowVersion 1 -> many WorkflowRuns
WorkflowRun 1 -> many NodeRunLogs
```

---

# 3. Module Architecture

## Top-Level Modules
```text
auth/
workflow/
execution/
run/
credential/
connector/
notification/
common/
config/
```

## Responsibilities

### auth
Registration, login, JWT auth, ownership context.

### workflow
CRUD workflows, save draft graph, publish lifecycle.

### execution
Worker engine, orchestration, retries, scheduling handoff.

### run
Run history, logs, monitoring APIs.

### credential
Secure storage of tokens/secrets.

### connector
Registry of node types/integrations.

### notification
Failure alerts / future user notifications.

### common
Shared response models, exceptions, utilities.

### config
Spring Boot infrastructure configuration.

---

# 4. Execution Engine

## Goal
Execute workflow graph reliably and asynchronously.

## Runtime Flow
```text
Trigger received
-> Create WorkflowRun
-> Push job to queue
-> Worker loads published version
-> Execute node by node
-> Persist logs
-> Complete run
```

## Node Traversal
Graph starts from trigger node.
Follow outgoing edges.
Condition nodes choose edge by label.

Example:
```text
Webhook -> Condition
true  -> Email
false -> Slack
```

## Execution Context
Shared mutable context passed between nodes.

```json
{
  "triggerData": {},
  "previousNodeOutput": {},
  "variables": {}
}
```

## Delay Node
Do not block thread.

Instead:
```text
mark run waiting
schedule resume job
resume later
```

## Retry Policy
Per node config:
```json
{
  "maxRetries": 3,
  "backoffSeconds": 5
}
```

---

# 5. Data Flow

## User Creates Workflow
```text
UI/API -> Workflow Module -> PostgreSQL
```

## User Publishes Workflow
```text
Draft validated -> Publish version -> Scheduler hooks activated
```

## Trigger Fires
```text
Webhook / Quartz -> API -> Run row -> Redis queue -> Worker
```

## Execution Completes
```text
Worker -> Node logs -> Run status -> Dashboard APIs
```

---

# 6. Schema Decisions

## Primary Database
**PostgreSQL (Supabase)**

Chosen for:
- ACID guarantees
- strong relational modeling
- indexing
- SQL analytics
- mature ecosystem

## JSONB Usage
Use JSONB for flexible data:
- node config
- connector config schema
- dynamic payload snapshots

Do NOT use JSONB when fixed columns are clearer.

## IDs
Use UUID primary keys.

Reason:
- safe public APIs
- harder to guess
- future distributed friendly

## Versioning Strategy
Use `workflow_versions` instead of cloning workflows.

Reason:
- one workflow identity
- audit history
- rollback path
- cleaner analytics

---

# 7. Patterns Used

## Strategy Pattern
Node execution by type.

Examples:
- EmailNodeExecutor
- DelayNodeExecutor
- ConditionNodeExecutor

## Factory Pattern
Resolve executor from node subtype.

```java
factory.get(nodeSubType);
```

## Domain Events
Loose coupling.

Examples:
- WorkflowPublishedEvent
- WorkflowRunCompletedEvent

## Builder Pattern
Complex DTO / config creation.

## Adapter Pattern
Third-party integrations.

---

# 8. Security Architecture

## Authentication
- JWT access token
- optional refresh token later

## Authorization
Resource ownership checks.
Users can access only own workflows.

## Secrets
Credentials encrypted at rest.

## Public Webhooks
Use secret path/token validation.
Rate limit via Kong.

---

# 9. Scaling Plan

## Current Phase (MVP)
```text
Single API service
Single Worker service
PostgreSQL
Redis
Quartz
```

## Scale Phase 1
```text
Multiple API replicas
Multiple Worker replicas
Read replicas
Redis tuning
```

## Scale Phase 2
```text
Dedicated queue system
Kafka / RabbitMQ
Separate execution service
Connector service extraction
```

## Scale Phase 3
```text
Multi-region
Tenant isolation
Advanced observability
```

---

# 10. Observability

## If Time Permits
Use:
- Prometheus metrics
- Grafana dashboards

Track:
- workflow runs per minute
- failure rate
- avg execution time
- queue depth
- retries count
- scheduler lag

---

# 11. Tradeoffs Chosen

## Modular Monolith over Microservices
Pros:
- faster delivery
- easier debugging
- lower ops cost

Tradeoff:
- less independent scaling initially

## PostgreSQL over NoSQL
Pros:
- relational fit
- ACID
- easier reporting

Tradeoff:
- schema discipline required

## Workflow Versions over Workflow Clones
Pros:
- cleaner identity
- easier auditing

Tradeoff:
- slightly more logic

## Whole Graph Save over Node CRUD APIs
Pros:
- simpler frontend sync
- fewer requests

Tradeoff:
- larger payloads

## Separate Worker Process
Pros:
- isolates long-running tasks
- scalable workers

Tradeoff:
- extra deployment unit

---

# 12. Future Roadmap

## Product
- Teams / workspaces
- RBAC
- Multi-draft support
- Auto-save drafts
- Visual builder UI
- Marketplace integrations
- Billing
- AI workflow generation

## Engineering
- Kafka event bus
- Distributed tracing
- Feature flags
- Canary deploys
- Horizontal sharding

---

# 13. Golden Rules

- Keep modules independent.
- Controllers stay thin.
- Services hold use cases.
- Execution engine remains stateless where possible.
- Prefer composition over inheritance.
- Optimize for clarity over