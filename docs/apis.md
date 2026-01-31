# APIs (REST) - v1

Base: /api/v1
Auth: Bearer accessToken (JWT)

## Auth
### POST /auth/register
Request:
- email
- password
- fullName (optional but recommended)

Response:
- userId
- accessToken
- refreshToken

### POST /auth/login
Request:
- email
- password
Response:
- accessToken
- refreshToken

### POST /auth/refresh
Request:
- refreshToken
Response:
- accessToken
- refreshToken (rotated)

### POST /auth/logout
Request:
- refreshToken
Response:
- 204

## Profile
### GET /me/profile
Response:
- profile data (minimal PII)

### PUT /me/profile
Request:
- fullName
- birthDate
- phone (optional)
- workplace (optional)

## Health
### GET /me/health
Response:
- bloodType
- allergies (count only optional)
- medications (count only optional)

### PUT /me/health
Request:
- bloodType
- medicalNotes (optional)

## Allergies
### GET /me/allergies
Response: list of {id, name, severity, notes? (optional)}
Note: consider hiding notes by default.

### POST /me/allergies
Request: {name, severity?, notes?}
Response: created

### DELETE /me/allergies/{id}
Response: 204

## Medications
### GET /me/medications
### POST /me/medications
### DELETE /me/medications/{id}

## Emergency Contacts
### GET /me/emergency-contacts
### POST /me/emergency-contacts
### PUT /me/emergency-contacts/{id}
### DELETE /me/emergency-contacts/{id}

## Addresses
### GET /me/addresses
### POST /me/addresses
### PUT /me/addresses/{id}
### DELETE /me/addresses/{id}

## Error Schema
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "details": { ... },
  "traceId": "..."
}

## Security Notes
- Never provide endpoints like GET /me/wallet returning everything.
- Consider rate limit on /auth/login.
- Consider email verification later (not MVP).
