# src/execution/AGENTS.md

## Scope
Workflow runtime engine, queue workers, node execution, retries.

## Rules
- Use Strategy Pattern for node executors.
- Use Factory to resolve executor by nodeSubType.
- Executors should be stateless.
- Persist node logs after each step.
- Never block threads for delay nodes.
- Use scheduler / requeue for waits.

## Preferred Classes
- WorkflowExecutionOrchestrator
- NodeExecutorFactory
- EmailNodeExecutor
- DelayNodeExecutor
- ConditionNodeExecutor
- RetryHandler

## Avoid
- HTTP controller logic
- UI concerns
- Large god services

---