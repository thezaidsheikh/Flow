# src/credential/AGENTS.md

## Scope
Secrets, API keys, connector credentials.

## Rules
- Encrypt at rest.
- Mask secrets in responses.
- Validate provider ownership.
- Rotate credentials safely later.

## Avoid
- Logging tokens
- Returning raw secrets

---