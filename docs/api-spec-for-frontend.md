# Especificação da API REST – Endpoints e Objetos

**Base URL:** `/api/v1`  
**Autenticação:** Header `Authorization: Bearer <accessToken>` (JWT).  
Endpoints em `/api/v1/me/*` exigem autenticação. `/api/v1/auth/*` e `/api/v1/health` são públicos.

---

## 1. Auth

### POST /api/v1/auth/register

**Request body:** `RegisterRequest`

| Campo     | Tipo   | Obrigatório | Regras                          |
|----------|--------|-------------|----------------------------------|
| email    | string | sim         | formato email, max 255           |
| password | string | sim         | 8–128 caracteres                 |
| fullName | string | não         | max 255                          |

**Response (201):** `AuthResponse`

| Campo        | Tipo   |
|-------------|--------|
| userId      | UUID   |
| accessToken | string |
| refreshToken| string |

---

### POST /api/v1/auth/login

**Request body:** `LoginRequest`

| Campo     | Tipo   | Obrigatório | Regras           |
|----------|--------|-------------|-------------------|
| email    | string | sim         | formato email     |
| password | string | sim         | —                 |

**Response (200):** `AuthResponse` (mesmo que register)

---

### POST /api/v1/auth/refresh

**Request body:** `RefreshRequest`

| Campo        | Tipo   | Obrigatório |
|-------------|--------|-------------|
| refreshToken| string | sim         |

**Response (200):** `RefreshResponse`

| Campo         | Tipo   |
|--------------|--------|
| accessToken  | string |
| refreshToken | string (rotacionado) |

---

### POST /api/v1/auth/logout

**Request body:** `LogoutRequest`

| Campo        | Tipo   | Obrigatório |
|-------------|--------|-------------|
| refreshToken| string | sim         |

**Response:** 204 No Content

---

## 2. Health (ping do servidor)

### GET /api/v1/health

Sem autenticação. Sem body.

**Response (200):** `HealthResponse`

| Campo  | Tipo   |
|--------|--------|
| status | string |

---

## 3. Perfil (requer auth)

### GET /api/v1/me/profile

**Response (200):** `ProfileResponse`

| Campo     | Tipo      |
|----------|-----------|
| fullName | string    |
| birthDate| date (ISO, ex: YYYY-MM-DD) |
| phone    | string    |
| workplace| string    |

---

### PUT /api/v1/me/profile

**Request body:** `UpdateProfileRequest`

| Campo     | Tipo   | Obrigatório | Regras   |
|----------|--------|-------------|----------|
| fullName | string | não         | max 255  |
| birthDate| date   | não         | ISO      |
| phone    | string | não         | max 64   |
| workplace| string | não         | max 255  |

**Response (200):** `ProfileResponse` (mesmo do GET)

---

## 4. Dados de saúde (requer auth)

### GET /api/v1/me/health

**Query params:**

| Nome          | Tipo    | Default | Descrição                          |
|---------------|---------|---------|------------------------------------|
| includeNotes  | boolean | false   | Se true, inclui medicalNotes       |

**Response (200):** `HealthInfoResponse`

| Campo          | Tipo    |
|----------------|---------|
| bloodType      | string  |
| allergyCount   | integer |
| medicationCount| integer |
| medicalNotes   | string  (só se includeNotes=true) |

**bloodType:** um de: `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`, `UNKNOWN`

---

### PUT /api/v1/me/health

**Request body:** `UpdateHealthRequest`

| Campo       | Tipo   | Obrigatório | Regras                                      |
|------------|--------|-------------|---------------------------------------------|
| bloodType  | string | não         | A+, A-, B+, B-, AB+, AB-, O+, O-, UNKNOWN   |
| medicalNotes| string| não         | max 2048                                    |

**Response (200):** `HealthInfoResponse` (mesmo do GET)

---

## 5. Alergias (requer auth)

### GET /api/v1/me/allergies

**Query params:**

| Nome         | Tipo    | Default | Descrição           |
|--------------|---------|---------|---------------------|
| includeNotes | boolean | false   | Incluir campo notes |

**Response (200):** array de `AllergyListItemResponse`

| Campo   | Tipo   |
|--------|--------|
| id     | UUID   |
| name   | string |
| severity| string (LOW, MEDIUM, HIGH ou vazio) |
| notes  | string (opcional/conforme includeNotes) |

---

### POST /api/v1/me/allergies

**Request body:** `CreateAllergyRequest`

| Campo    | Tipo   | Obrigatório | Regras              |
|----------|--------|-------------|---------------------|
| name     | string | sim         | max 255             |
| severity | string | não         | LOW, MEDIUM, HIGH   |
| notes    | string | não         | max 2048            |

**Response (201):** `AllergyListItemResponse` (um item)

---

### PUT /api/v1/me/allergies/{id}

**Path:** `id` = UUID da alergia

**Request body:** `UpdateAllergyRequest`

| Campo    | Tipo   | Obrigatório | Regras              |
|----------|--------|-------------|---------------------|
| name     | string | não         | max 255             |
| severity | string | não         | LOW, MEDIUM, HIGH   |
| notes    | string | não         | max 2048            |

**Response (200):** `AllergyListItemResponse`

---

### DELETE /api/v1/me/allergies/{id}

**Path:** `id` = UUID da alergia

**Response:** 204 No Content

---

## 6. Medicamentos (requer auth)

### GET /api/v1/me/medications

**Query params:**

| Nome         | Tipo    | Default | Descrição           |
|--------------|---------|---------|---------------------|
| includeNotes | boolean | false   | Incluir campo notes |

**Response (200):** array de `MedicationListItemResponse`

| Campo    | Tipo   |
|----------|--------|
| id       | UUID   |
| name     | string |
| dosage   | string |
| frequency| string |
| notes    | string (opcional/conforme includeNotes) |

---

### POST /api/v1/me/medications

**Request body:** `CreateMedicationRequest`

| Campo     | Tipo   | Obrigatório | Regras  |
|-----------|--------|-------------|---------|
| name      | string | sim         | max 255 |
| dosage    | string | não         | max 128 |
| frequency | string | não         | max 128 |
| notes     | string | não         | max 2048|

**Response (201):** `MedicationListItemResponse`

---

### PUT /api/v1/me/medications/{id}

**Path:** `id` = UUID do medicamento

**Request body:** `UpdateMedicationRequest` (mesmos campos de create, todos opcionais)

**Response (200):** `MedicationListItemResponse`

---

### DELETE /api/v1/me/medications/{id}

**Path:** `id` = UUID do medicamento

**Response:** 204 No Content

---

## 7. Contatos de emergência (requer auth)

### GET /api/v1/me/emergency-contacts

**Response (200):** array de `EmergencyContactResponse`

| Campo       | Tipo    |
|-------------|---------|
| id          | UUID    |
| name        | string  |
| relationship| string  |
| phone       | string  |
| priority    | integer |

---

### POST /api/v1/me/emergency-contacts

**Request body:** `CreateEmergencyContactRequest`

| Campo       | Tipo    | Obrigatório | Regras  |
|-------------|---------|-------------|---------|
| name        | string  | sim         | max 255 |
| relationship| string  | não         | max 128 |
| phone       | string  | sim         | max 64  |
| priority    | integer | não         | —       |

**Response (201):** `EmergencyContactResponse`

---

### PUT /api/v1/me/emergency-contacts/{id}

**Path:** `id` = UUID do contato

**Request body:** `UpdateEmergencyContactRequest` (mesmos campos do create, todos opcionais)

**Response (200):** `EmergencyContactResponse`

---

### DELETE /api/v1/me/emergency-contacts/{id}

**Path:** `id` = UUID do contato

**Response:** 204 No Content

---

## 8. Endereços (requer auth)

### GET /api/v1/me/addresses

**Response (200):** array de `AddressResponse`

| Campo     | Tipo    |
|-----------|---------|
| id        | UUID    |
| label     | string  |
| isPrimary | boolean |
| street    | string  |
| number    | string  |
| city      | string  |
| state     | string  |
| zip       | string  |
| country   | string  |

**label:** um de: `HOME`, `WORK`, `OTHER`

---

### POST /api/v1/me/addresses

**Request body:** `CreateAddressRequest`

| Campo     | Tipo    | Obrigatório | Regras        |
|-----------|---------|-------------|---------------|
| label     | string  | sim         | HOME, WORK, OTHER |
| isPrimary | boolean | não         | —             |
| street    | string  | não         | max 512       |
| number    | string  | não         | max 64        |
| city      | string  | não         | max 128       |
| state     | string  | não         | max 128       |
| zip       | string  | não         | max 64        |
| country   | string  | não         | max 128       |

**Response (201):** `AddressResponse`

---

### PUT /api/v1/me/addresses/{id}

**Path:** `id` = UUID do endereço

**Request body:** `UpdateAddressRequest` (mesmos campos do create, todos opcionais)

**Response (200):** `AddressResponse`

---

### DELETE /api/v1/me/addresses/{id}

**Path:** `id` = UUID do endereço

**Response:** 204 No Content

---

## 9. Resposta de erro (4xx / 5xx)

**Body:** `ErrorResponse`

| Campo   | Tipo            |
|---------|-----------------|
| code    | string          |
| message | string          |
| details | object (opcional; ex.: erros de validação por campo) |
| traceId | string (opcional) |

Exemplo:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "details": { "field": "email", "reason": "Invalid email format" },
  "traceId": "..."
}
```

Códigos comuns: `VALIDATION_ERROR`, `UNAUTHORIZED`, `INVALID_TOKEN`, `TOKEN_EXPIRED`, etc.

---

## 10. Resumo por recurso

| Recurso              | GET (lista/detalhe)     | POST (criar) | PUT (atualizar)     | DELETE      |
|----------------------|-------------------------|--------------|----------------------|-------------|
| auth                 | —                       | register, login, refresh, logout | — | —       |
| health (servidor)    | /health                 | —            | —                    | —           |
| profile              | /me/profile             | —            | /me/profile          | —           |
| health (usuário)     | /me/health              | —            | /me/health           | —           |
| allergies            | /me/allergies           | /me/allergies| /me/allergies/{id}   | /me/allergies/{id} |
| medications          | /me/medications         | /me/medications | /me/medications/{id} | /me/medications/{id} |
| emergency-contacts   | /me/emergency-contacts  | /me/emergency-contacts | /me/emergency-contacts/{id} | /me/emergency-contacts/{id} |
| addresses            | /me/addresses           | /me/addresses| /me/addresses/{id}   | /me/addresses/{id} |

Todos os IDs em path são **UUID**. Datas em **ISO 8601** (ex.: `YYYY-MM-DD` para birthDate).  
Para listas, o frontend pode assumir array JSON; paginação pode ser adicionada depois conforme regras do projeto.
