# Flow - Workflow Automation Platform

## Overview
Flow is a backend-first workflow automation platform inspired by tools like Zapier, Make, and n8n. It enables users to build, publish, execute, and monitor automated workflows using triggers, conditions, delays, and actions.

This project is designed as a **production-minded capstone MVP** with clean architecture, modular monolith design, and future SaaS scalability.

### Example Workflow
Webhook Trigger -> Condition Check -> Delay -> Send Email

### Current MVP Goals
- User authentication and workflow ownership
- Draft and publish workflows
- Execute workflows asynchronously
- Webhook and scheduled triggers
- Run history and node-level logs
- Modular connector-ready architecture
- Deployable production setup

---

## Tech Stack

### Backend
- **Java 21** - Modern Java features, concurrency, strong OOP support
- **Spring Boot** - Mature enterprise ecosystem
- **Spring Security** - JWT authentication / authorization
- **Spring Data JPA / Hibernate** - ORM layer

### Database
- **PostgreSQL** - Relational, ACID compliant
- **JSONB** - Flexible storage for node configs and graph metadata
- **Supabase** - Managed PostgreSQL cloud hosting

### Async / Scheduling / Performance
- **Redis** - Caching, queue coordination, temporary state
- **Quartz Scheduler** - Cron / timer workflow scheduling
- **Separate Worker Process** - Background workflow execution

### Gateway / Infra
- **Kong Gateway** - API Gateway / Load Balancer / Rate limiting
- **Docker / Docker Compose** - Local development & deployment

### Observability (If Time Permits)
- **Grafana** - Dashboards / Monitoring
- **Prometheus** - Metrics scraping and alerting

---

## How to Run

## Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Supabase / PostgreSQL instance
- Redis instance

## Environment Variables
Create `.env` or `application-local.yml`

```env
DB_URL=jdbc:postgresql://localhost:5432/flowforge
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change_me
REDIS_HOST=localhost
REDIS_PORT=6379
```

## Start Infrastructure
```bash
docker compose up -d
```

## Run API Server
```bash
./mvnw spring-boot:run
```

## Run Worker Profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=worker
```

## Build Project
```bash
./mvnw clean package
```

---

## Modules

```text
flowforge/
├── auth/          # login, JWT, user identity
├── workflow/      # workflow CRUD, draft, publish, graph management
├── execution/     # engine, worker, node executors, retries
├── run/           # execution history, logs, run APIs
├── connector/     # integration metadata / plugin registry
├── credential/    # encrypted tokens / secrets
├── notification/  # alerts / failure notifications
├── common/        # shared utilities / response wrappers / exceptions
├── config/        # Spring configs
```

---

## Useful Commands

## Run Tests
```bash
./mvnw test
```

## Run Specific Profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Lint / Verify
```bash
./mvnw verify
```

## Generate Docs
```bash
./mvnw javadoc:javadoc
```

---

## API Docs

### Swagger UI
```text
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON
```text
http://localhost:8080/v3/api-docs
```

### Core API Groups

#### Auth
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- GET /api/v1/auth/me

#### Workflow
- POST /api/v1/workflows
- GET /api/v1/workflows
- GET /api/v1/workflows/{id}
- PUT /api/v1/workflows/{id}/draft
- POST /api/v1/workflows/{id}/publish
- POST /api/v1/workflows/{id}/run

#### Runs
- GET /api/v1/workflows/{id}/runs
- GET /api/v1/runs/{runId}
- GET /api/v1/runs/{runId}/logs

#### Public Hooks
- POST /hooks/{workflowId}/{secret}

---

## Folder Structure

```text
src/main/java/com/flowforge
├── common/
├── config/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/
├── workflow/
├── execution/
├── run/
├── connector/
├── credential/
└── notification/
```

### Module Pattern
Each module follows:

```text
controller -> HTTP layer
service    -> business use cases
repository -> persistence
entity     -> domain models
dto        -> requests/responses
validator  -> rules
mapper     -> transformations
```

---

## Roadmap
### MVP
- Auth
- Workflow CRUD
- Draft + Publish
- Webhook trigger
- Scheduler trigger
- Condition / Delay / Email nodes
- Run history
- Logs

### Future Enhancements
- Teams / Workspaces
- RBAC
- Multiple drafts
- Visual drag-drop builder
- Marketplace connectors
- Billing / subscriptions
- Real-time run monitoring
- Kafka event bus
- Horizontal worker scaling
- AI workflow generation

### Product Features
- Visual drag-drop workflow builder UI
- Multi-user teams / workspaces
- RBAC permissions
- Multiple drafts / autosave
- Templates marketplace
- Billing / subscriptions
- Public workflow sharing
- Real-time execution monitoring
- WebSocket updates
- AI workflow generation assistant

### Integrations
- Slack
- Gmail
- Twilio
- Google Sheets
- Stripe
- Salesforce
- Notion
- Webhooks

### Engineering Enhancements
- Kafka / RabbitMQ event bus
- Horizontal worker scaling
- Distributed tracing
- Rate limiting policies
- Feature flags
- Canary deployments
- Multi-region failover

### Observability
- Grafana dashboards
- Prometheus alerts
- Error budgets / SLOs

---
## 🤝 Engineering Principles

- SOLID Principles
- Separation of Concerns
- Feature-based modules
- Standardized API contracts
- Clean Architecture mindset
- Extensible node executor model

---

## 📄 Docs

- `README.md` → onboarding
- `ARCHITECTURE.md` → system design truth
- `AGENTS.md` → AI coding rules
- `src/**/AGENTS.md` → local module rules

---

## Project Philosophy
Build a clean, simple, production-grade MVP first.
Optimize for maintainability, scalability, and future SaaS growth.
Avoid overengineering while keeping strong foundations.

# 🏁 Status
Currently in active development as capstone project with production-grade engineering standards.