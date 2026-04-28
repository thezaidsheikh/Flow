# src/run/AGENTS.md

## Scope
Run history, logs, monitoring APIs.

## Rules
- Read optimized queries.
- Always paginate run listings.
- Separate run summary from logs endpoints.
- Include timestamps and status transitions.

## Preferred Services
- GetRunService
  n- ListWorkflowRunsService
- GetRunLogsService

---