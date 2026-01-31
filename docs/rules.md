# Rules (Backend)

## Product Goal
Build a secure “personal wallet” of critical user information (health, emergency contacts, addresses, documents metadata), accessible anywhere through authenticated access, with future support for an emergency read-only mode.

## Non-negotiables
- Security first: this is sensitive data.
- MVP must be small, coherent, and evolvable.
- No endpoint returns “everything”.
- Never log sensitive fields (PII/PHI).
- Use versioned API routes from day 1.

## Architecture Constraints (Layered)
- Controllers must contain no business rules.
- Services contain business rules and orchestrate repositories.
- Repositories are persistence-only (no business logic).
- DTOs are used in Controllers; Entities live in persistence layer.
- Mapping between DTO <-> Model happens in Services (or dedicated Mapper).

## Authentication & Authorization
- Auth via email + password (MVP).
- Passwords must be hashed with BCrypt (never store plain).
- JWT access token short-lived (e.g., 15m) + refresh token longer-lived (e.g., 30d).
- Refresh token must be stored hashed in DB and rotated on use.
- All user data endpoints require authentication.
- Enforce ownership: user can only access their own resources.

## Data Sensitivity Classification
- P0 (Critical): allergies, medications, blood type, emergency contacts.
- P1 (Sensitive): address, workplace, documents references.
- P2 (Standard): preferences, display data.
Rules:
- P0/P1 fields should be encrypted at rest (application-level encryption preferred).
- Avoid returning P0/P1 unless explicitly requested by endpoint.

## Encryption (At Rest)
- Use AES-GCM for application-level encryption of sensitive fields.
- Store encryption key outside codebase (env/secret manager).
- Consider per-user derived keys in future (KMS/Keychain-like model).
- Never return raw encryption details.

## Validation Rules (Examples)
- BloodType: must be one of [A+, A-, B+, B-, AB+, AB-, O+, O-] or unknown.
- Allergy: name required, optionally severity.
- EmergencyContact: name + phone required.
- Address: minimum fields required when marked as primary.

## Auditing & Observability
- Store audit events for sensitive reads/updates:
  - what: resource type + action
  - who: user id
  - when: timestamp
  - where: client info if available
- Logs:
  - never include PII/PHI
  - include request id / correlation id

## Error Handling
- Use consistent error schema:
  - code, message, details(optional), traceId
- Do not leak internals (SQL, stack traces) in production.

## API Design
- RESTful, JSON.
- Versioned routes: /api/v1/...
- Pagination for list endpoints.
- Idempotency for updates where possible.

## Testing Minimum
- Unit tests for Services (business rules).
- Integration tests for Auth + one critical data flow.
- Contract tests for DTO schemas (optional for MVP).

## Future: Emergency Mode (Design Rule)
- Emergency access must be:
  - read-only
  - limited dataset (P0 only + minimal identity)
  - time-limited token
  - explicit user opt-in
- Emergency mode must have separate endpoints and separate tokens.
