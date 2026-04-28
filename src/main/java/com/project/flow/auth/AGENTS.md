# src/auth/AGENTS.md

## Scope
Authentication, authorization, JWT, user identity.

## Rules
- Use Spring Security best practices.
- JWT for access tokens.
- BCrypt for passwords.
- Validate ownership in service layer.
- Never expose sensitive fields.

## Preferred Services
- RegisterUserService
- LoginUserService
- TokenService
- CurrentUserService

## Avoid
- Business workflow logic here
- Plain text secrets

---