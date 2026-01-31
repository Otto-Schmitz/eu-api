# Prompts (Cursor / AI) - Backend Generation

## How to Use
1. Paste these docs into backend/docs exactly.
2. In Cursor, open backend/ as project root.
3. Use the prompts below in order. Each prompt assumes layered architecture and the rules in rules.md.

---

## Prompt 1 — Project Bootstrap
Create a backend API using layered architecture:
- Controllers, Services, Repositories, Entities, DTOs, Security, Crypto, Exceptions
- PostgreSQL persistence
- JWT auth (access + refresh with rotation)
- AES-GCM encryption service for sensitive fields
- Global exception handler + consistent error schema
Output complete runnable project.

Constraints:
- Follow backend/docs/rules.md strictly.
- Follow endpoints defined in backend/docs/apis.md.
- No endpoint returns all user wallet data at once.

---

## Prompt 2 — Auth Module
Implement:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout

Details:
- BCrypt password hashing
- JWT access token short TTL
- Refresh token stored hashed, rotated on refresh
- Repository + entity for refresh tokens
- Rate limiting placeholder for login (interface or simple in-memory for now)
- Tests for auth flows

---

## Prompt 3 — Profile Module
Implement:
- GET /api/v1/me/profile
- PUT /api/v1/me/profile

Include:
- DTOs
- Service validations
- Encryption for workplace field
- Ownership enforced via token userId

---

## Prompt 4 — Health Module
Implement:
- GET /api/v1/me/health
- PUT /api/v1/me/health
- Allergies CRUD
- Medications CRUD

Include:
- BloodType enum validation
- Medical notes encrypted
- Avoid returning encrypted fields unless requested

---

## Prompt 5 — Emergency Contacts + Addresses
Implement:
- Emergency contacts CRUD with priority ordering
- Addresses CRUD with primary address constraint (only one primary)

Include:
- Encrypt address lines, number, zip
- Service enforces one primary address at a time

---

## Prompt 6 — Audit Events (Minimal)
Implement:
- AuditEventEntity + repository
- Service writes audit events for:
  - reading/updating health, allergies, medications
  - reading/updating emergency contacts
Do not log sensitive values.

---

## Prompt 7 — Hardening Checklist
Add:
- CORS config
- Security config hardened
- Token validation filter
- Consistent error mapping
- Basic integration tests
- Dockerfile + docker-compose (app + postgres)
<!--  -->