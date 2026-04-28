# src/connector/AGENTS.md

## Scope
Connector metadata and integration registry.

## Rules
- Store config schema.
- Use Adapter Pattern for external providers.
- Keep connector definitions extensible.
- Add new connectors without changing engine core.

## Preferred Classes
- ConnectorRegistry
- SlackConnectorAdapter
- EmailConnectorAdapter

---