# Style Guide (Backend)

## General
- Keep endpoints small and explicit.
- Prefer explicit resources over "mega DTOs".
- Prefer clear naming over abbreviations.

## Code Style
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE
- Variables/Objects: camelCase

## Naming
- Controllers: XController
- Services: XService
- Repositories: XRepository
- DTOs:
  - request: CreateXRequest, UpdateXRequest
  - response: XResponse, XListItemResponse

## HTTP Status
- 200 for reads
- 201 for creates
- 204 for deletes/logout
- 400 validation
- 401 unauthenticated
- 403 unauthorized (ownership)
- 404 not found

## DTO Rules
- Controllers accept/return DTOs only.
- Entities never leave the service layer.
- Avoid exposing internal ids when not needed, but ok for list management.

## Lombok
Use in:
- Domain
- DTOs
- Entities

Prefer:
- @Getter @Setter
- @Builder
- @NoArgsConstructor
- @AllArgsConstructor
- @Data

## Interfaces
- Every service must have an interface.
- Implementation always in service.impl.

## Repositories
- Extend JpaRepository.
- No custom logic unless strictly query-related.

## Mappers
Must use Builder.

## Validation
- Basic validation at DTO level (required fields, formats).
- Business validation at Service level.

## Logging
- Log:
  - requestId/traceId
  - endpoint
  - status code
  - latency
- Never log:
  - email in plain (optional: hash it)
  - addresses, medical notes, document ids
  - tokens

## Commits
Must be clean and short:
- feat: (new feature description)
- fix: (bug fix description)
- refactor: (refactor only description)
- docs: (documentation description)

## Security Headers (future)
- HSTS, CSP (if serving UI), etc. Not required for API-only MVP.

## Documentation
- Keep docs in /backend/docs updated.
- Keep an OpenAPI spec later (not required immediately).
