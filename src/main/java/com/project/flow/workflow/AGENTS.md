# src/workflow/AGENTS.md

## Scope
Workflow creation, draft management, publish lifecycle, graph validation.

## Rules
- Keep workflow as business container.
- Use WorkflowVersion for draft/published states.
- Save whole graph snapshot from UI.
- Validate graph before publish.
- Controllers thin, use-case services preferred.

## Preferred Services
- CreateWorkflowService
- SaveDraftService
- PublishWorkflowService
- GetWorkflowService

## Avoid
- Execution logic here
- Direct cross-module repository access

---