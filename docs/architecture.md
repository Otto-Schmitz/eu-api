# Architecture (Layered / N-tier)

## Overview
We use a classic layered architecture:
- Presentation (Controllers): HTTP layer, DTO validation, authentication context extraction.
- Application (Services): business logic, orchestration, mapping DTO <-> domain models, transactions.
- Data Access (Repositories): persistence-only (CRUD, queries).
- Infrastructure (Config/Security/Crypto): JWT, encryption, database config, etc.

## Layer Responsibilities

### Controllers (Presentation)
- Receive and validate request DTOs
- Call Services
- Return response DTOs
- No business rules
- No persistence calls directly

### Services (Application)
- Implement business use cases
- Validate domain rules beyond basic DTO validation
- Enforce authorization and ownership checks
- Coordinate repositories and crypto
- Map between:
  - DTOs (Controller-facing)
  - Models (Service-facing)
  - Entities (Repository-facing)

### Repositories (Data Access)
- CRUD & query composition
- No business logic, no authorization logic
- Return Entities (or mapped models if you prefer, but keep consistent)

### Infrastructure
- Security: JWT issuing/validation, password hashing, refresh rotation
- Crypto: AES-GCM encryption/decryption utilities
- Config: environment-based properties
- Persistence: ORM mapping, migrations

## Suggested Package Layout (Java example)
- controller/
- contract/
- dto/
- service/
- repository/
- entity/
- security/
- security/crypto/
- security/config/
- exception/

## Transactions
- Transactions are handled at Service layer.
- Controllers never start transactions.

## Authentication Flow (MVP)
1. User registers (email + password)
2. Password hashed (BCrypt)
3. Login returns:
   - accessToken (JWT, short TTL)
   - refreshToken (opaque random string, stored hashed in DB)
4. Refresh rotates refresh token (invalidate old, issue new)
5. Logout revokes refresh token

## Authorization Model
- Single-tenant: each user only sees own data.
- Every request derives userId from access token.
- Each resource is keyed by userId (or has userId foreign key).

## Data Model Approach
- Keep the DB normalized:
  - user
  - user_profile (1:1)
  - health_info (1:1)
  - allergies (1:n)
  - medications (1:n)
  - emergency_contacts (1:n)
  - addresses (1:n)
  - documents (1:n)
  - refresh_tokens (1:n)
  - audit_events (1:n)
- Sensitive fields encrypted at application layer:
  - address lines, workplace, medical notes, document numbers (if stored)

## Performance & Scalability (Pragmatic)
- Start with monolith API + PostgreSQL.
- Add caching later only if needed.
- Index by user_id across all tables.

## Versioning & Evolution
- API routes versioned (/api/v1).
- DB migrations required for changes.
- Add new categories as new tables or as “typed records” (future).
