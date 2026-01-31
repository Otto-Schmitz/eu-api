# Project Structure (Backend)

## Directories
backend/
  docs/
    rules.md
    architecture.md
    structure.md
    domain.md
    apis.md
    style.md
    prompts.md
  src/
    main/
      java/... (or language equivalent)
    test/
  README.md

## Suggested Code Packages (Java)
- config/
  - AppConfig, JacksonConfig, CorsConfig
- security/
  - JwtService, AuthFilter, SecurityConfig
  - PasswordHasher (BCrypt)
- crypto/
  - CryptoService (AES-GCM), KeyProvider
- controller/
  - AuthController
  - ProfileController
  - HealthController
  - EmergencyContactsController
  - AddressesController
- dto/
  - request/...
  - response/...
- service/
  - AuthService
  - ProfileService
  - HealthService
  - EmergencyContactService
  - AddressService
- repository/
  - UserRepository
  - RefreshTokenRepository
  - ProfileRepository
  - HealthRepository
  - AllergyRepository
  - MedicationRepository
  - EmergencyContactRepository
  - AddressRepository
  - AuditEventRepository
- entity/
  - UserEntity, RefreshTokenEntity, ...
- exception/
  - ApiException, NotFoundException, ForbiddenException, ValidationException
  - GlobalExceptionHandler
- mapper/ (optional but recommended)
  - ProfileMapper, HealthMapper, ...

## Environment Variables (MVP)
- DATABASE_URL
- DATABASE_USERNAME
- DATABASE_PASSWORD
- JWT_SECRET
- JWT_ISSUER
- ACCESS_TOKEN_TTL_MINUTES
- REFRESH_TOKEN_TTL_DAYS
- CRYPTO_MASTER_KEY (base64)
- LOG_LEVEL

## Build/Run (example)
- dev: local postgres + app
- prod: containerized + secrets injected
