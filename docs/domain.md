# Domain

## Core Concept
User owns a “wallet” of critical personal info.

## Entities (Conceptual)
- User
  - id
  - email
  - passwordHash
  - status (ACTIVE)
  - createdAt, updatedAt
- UserProfile
  - userId (FK)
  - fullName
  - birthDate
  - phone (optional)
  - workplace (optional, encrypted)
  - createdAt, updatedAt
- HealthInfo
  - userId (FK)
  - bloodType
  - medicalNotes (optional, encrypted)
- Allergy
  - id
  - userId (FK)
  - name
  - severity (LOW/MEDIUM/HIGH) (optional)
  - notes (optional, encrypted)
- Medication
  - id
  - userId (FK)
  - name
  - dosage (optional)
  - frequency (optional)
  - notes (optional, encrypted)
- EmergencyContact
  - id
  - userId (FK)
  - name
  - relationship (optional)
  - phone
  - priority (int)
- Address
  - id
  - userId (FK)
  - label (HOME/WORK/OTHER)
  - isPrimary
  - street (encrypted)
  - number (encrypted)
  - city
  - state
  - zip (encrypted)
  - country
- DocumentRef (MVP: only metadata; no file upload)
  - id
  - userId (FK)
  - type (RG/CPF/PASSPORT/INSURANCE/OTHER)
  - identifier (encrypted) (optional)
  - notes (encrypted) (optional)

## Business Rules
- Each user has exactly one profile (create on registration).
- bloodType is validated against a known enum or UNKNOWN.
- Emergency contacts are ordered by priority; at least one recommended (not required).
- No resource can be accessed if userId != token.userId.
- Sensitive fields:
  - workplace, address lines, medical notes, document identifiers
  must be encrypted at rest.
- Deleting user should cascade or soft-delete (MVP choose soft-delete).

## Use Cases (MVP)
- Register user
- Login
- Refresh token
- Logout
- Get my profile
- Update my profile
- Get health info
- Update health info
- Add/list/delete allergies
- Add/list/delete medications
- Add/list/update/delete emergency contacts
- Add/list/update/delete addresses

## Future Use Cases (Not now)
- Emergency mode link/token
- Share a subset for limited time
- Export wallet to PDF
- Attach documents (files)
- Multi-device sync with conflict resolution
