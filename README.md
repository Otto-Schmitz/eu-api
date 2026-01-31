# eu-api

Personal wallet API — health info, allergies, medications, emergency contacts, addresses. Layered backend: **Controller → Service → Repository → Entity**. No business logic or persistence in controllers; DTOs only at the HTTP boundary.

## Stack

- **Java 21**, Spring Boot 3.2
- **PostgreSQL**, Spring Data JPA
- **JWT** (access + refresh), **AES-GCM** for sensitive fields (workplace, medical notes, address lines, allergy/medication notes)
- **Maven**

---

## How to run locally

### Option 1: Maven (with local Postgres)

1. Create a database and user:

   ```bash
   createdb eu
   # ensure user has access, or use default postgres user
   ```

2. Set environment variables (or use defaults):

   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/eu
   export DATABASE_USERNAME=eu
   export DATABASE_PASSWORD=eu
   export JWT_SECRET=your-256-bit-secret-at-least-32-characters-long
   export CRYPTO_MASTER_KEY=base64-encoded-32-bytes  # e.g. openssl rand -base64 32
   ```

3. Run:

   ```bash
   mvn spring-boot:run
   ```

   Or build and run the JAR:

   ```bash
   mvn clean package
   java -jar target/eu-api-1.0.0-SNAPSHOT.jar
   ```

   API: **http://localhost:8080**

### Option 2: Docker (API + Postgres)

1. Copy env example and set required vars:

   ```bash
   cp .env.example .env
   # Edit .env: set JWT_SECRET and CRYPTO_MASTER_KEY for production
   ```

2. Start stack:

   ```bash
   docker compose up --build
   ```

   API: **http://localhost:8080**  
   Postgres: **localhost:5432** (user `eu`, password from `POSTGRES_PASSWORD` or default `eu`).

---

## Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| **Database** | | |
| `DATABASE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/eu` |
| `DATABASE_USERNAME` | DB user | `eu` |
| `DATABASE_PASSWORD` | DB password | — |
| **Security** | | |
| `JWT_SECRET` | HMAC key for access tokens (min 256 bits) | — |
| `JWT_ISSUER` | Token issuer claim | `eu-api` |
| `ACCESS_TOKEN_TTL_MINUTES` | Access token TTL | `15` |
| `REFRESH_TOKEN_TTL_DAYS` | Refresh token TTL | `30` |
| `CRYPTO_MASTER_KEY` | Base64 AES-256 key for sensitive fields | — (placeholder in dev) |
| **Server** | | |
| `SERVER_PORT` | HTTP port | `8080` |
| `MAX_HTTP_POST_SIZE` | Max request body (bytes) | `1048576` (1MB) |
| `MAX_BODY_SIZE` | Max in-memory body (bytes) | `1048576` |
| **CORS** | | |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins | `*` (avoid in prod) |
| `CORS_MAX_AGE` | Preflight cache (seconds) | `86400` |
| **Rate limiting** | | |
| `RATE_LIMIT_AUTH_PER_MINUTE` | Auth endpoints limit per IP | `10` |
| `RATE_LIMIT_AUTH_BURST` | Burst size | `5` |
| **Cleanup** | | |
| `REFRESH_TOKEN_CLEANUP_ENABLED` | Run refresh token cleanup job | `true` |
| `REFRESH_TOKEN_CLEANUP_RETAIN_DAYS` | Delete tokens expired longer than (days) | `7` |
| `REFRESH_TOKEN_CLEANUP_CRON` | Cron expression | `0 0 3 * * ?` (03:00 daily) |
| **Logging** | | |
| `LOG_LEVEL` | Root / eu.api log level | `INFO` |

---

## Main endpoints

Base path: **`/api/v1`**. Auth: **Bearer** access token (except health and auth routes).

### Public (no auth)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| POST | `/auth/register` | Register (email, password, fullName?) |
| POST | `/auth/login` | Login (email, password) |
| POST | `/auth/refresh` | Rotate refresh token |
| POST | `/auth/logout` | Invalidate refresh token |

### Protected (JWT required)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/me/profile` | Get my profile |
| PUT | `/me/profile` | Update profile (fullName, birthDate, phone?, workplace?) |
| GET | `/me/health` | Get health info (?includeNotes=false) |
| PUT | `/me/health` | Update health (bloodType?, medicalNotes?) |
| GET | `/me/allergies` | List allergies (?includeNotes=false) |
| POST | `/me/allergies` | Create allergy |
| PUT | `/me/allergies/{id}` | Update allergy |
| DELETE | `/me/allergies/{id}` | Delete allergy |
| GET | `/me/medications` | List medications (?includeNotes=false) |
| POST | `/me/medications` | Create medication |
| PUT | `/me/medications/{id}` | Update medication |
| DELETE | `/me/medications/{id}` | Delete medication |
| GET | `/me/emergency-contacts` | List emergency contacts |
| POST | `/me/emergency-contacts` | Create contact |
| PUT | `/me/emergency-contacts/{id}` | Update contact |
| DELETE | `/me/emergency-contacts/{id}` | Delete contact |
| GET | `/me/addresses` | List addresses |
| POST | `/me/addresses` | Create address |
| PUT | `/me/addresses/{id}` | Update address |
| DELETE | `/me/addresses/{id}` | Delete address |

### Actuator (no auth for health/info)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/actuator/health` | Spring Boot health |
| GET | `/actuator/info` | App info |

---

## Error responses

JSON shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "details": { "field": "message" },
  "traceId": "uuid"
}
```

Common codes: `VALIDATION_ERROR`, `UNAUTHENTICATED`, `FORBIDDEN`, `TOKEN_EXPIRED`, `INVALID_TOKEN`, `INVALID_REFRESH_TOKEN`, `RATE_LIMIT_EXCEEDED`, `INTERNAL_ERROR`.

---

## Project structure

- **config/** — CORS, Jackson, refresh token cleanup scheduler
- **controller/** — HTTP only; DTOs in/out; `CurrentUser` for userId
- **service/** — interfaces; **service.impl/** — business logic, encryption, audit
- **repository/** — JPA repositories
- **entity/** — JPA entities
- **dto/request**, **dto/response** — request/response DTOs (validation on requests)
- **crypto/** — AES-GCM for sensitive fields
- **exception/** — ApiException, GlobalExceptionHandler
- **security/** — JWT, auth filter, rate limit filter, secure headers

See **docs/** for architecture, domain, APIs, and style.
