# eu-api

Personal wallet API — health, emergency contacts, addresses. Layered backend (Controller → Service → Repository → Entity).

## Stack

- Java 21, Spring Boot 3.2
- PostgreSQL, Spring Data JPA
- JWT (access + refresh), AES-GCM for sensitive fields
- Maven

## Build & Run

```bash
mvn clean package
java -jar target/eu-api-1.0.0-SNAPSHOT.jar
```

Or with Maven:

```bash
mvn spring-boot:run
```

## Environment (MVP)

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | JDBC URL (default: `jdbc:postgresql://localhost:5432/eu`) |
| `DATABASE_USERNAME` | DB user |
| `DATABASE_PASSWORD` | DB password |
| `LOG_LEVEL` | Root log level (default: INFO) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins (default: `*`) |

## Endpoints (bootstrap)

- `GET /api/v1/health` — health check (no auth)
- `GET /actuator/health` — Spring Boot health (no auth)
- `GET /actuator/info` — app info (no auth)

All other `/api/v1/**` routes require authentication (JWT to be implemented).

## Project structure

- `config/` — App, Jackson, CORS
- `controller/` — HTTP layer, DTOs only
- `service/` — interfaces; `service.impl/` — implementations
- `repository/` — JPA repositories
- `entity/` — JPA entities
- `dto/request`, `dto/response` — DTOs
- `exception/` — ApiException, GlobalExceptionHandler
- `security/` — Security config, request ID filter

See `docs/` for architecture, domain, APIs, and style.
