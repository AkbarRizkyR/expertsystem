# Expert System API - Dokumentasi Endpoint

Dokumentasi lengkap seluruh endpoint REST.

- **Base URL** (default): `http://localhost:8080`
- **Method**: hanya `GET` dan `POST`.
- **Content-Type**: `application/json`, kecuali `POST /api/scan` (`multipart/form-data`).
- **Format error** (seragam, JSON):
  - Umum: `{ "error": "pesan" }`
  - Validasi: `{ "error": "Validasi gagal", "fields": { "namaField": "pesan" } }`

## Daftar isi

- [Autentikasi & Otorisasi](#autentikasi--otorisasi)
- [1. Auth](#1-auth)
- [2. Scan](#2-scan)
- [3. Applications](#3-applications)
- [4. Unmatched Queue](#4-unmatched-queue-security-reviewer)
- [5. Knowledge Rules](#5-knowledge-rules-security-reviewer)
- [6. Users](#6-users-admin)
- [7. Frameworks](#7-frameworks)
- [8. Utility](#8-utility--non-api)
- [Skema Data (Model)](#skema-data-model)
- [Kode Status HTTP](#kode-status-http)

---

## Autentikasi & Otorisasi

Autentikasi memakai **JWT (Bearer token)**. Login dulu untuk memperoleh token, lalu kirim di setiap request yang butuh role:

```
Authorization: Bearer <token>
```

Isi klaim token: `sub` = user id, `groups` = nama role, `uid` = user id, `name` = nama, `upn` = email, `exp` = kedaluwarsa (default 8 jam, diatur `jwt.duration.seconds`).

### Role & akses

| Role | `role_id` | Akses |
|------|:--------:|-------|
| `DEVELOPER` | 1 | scan + kelola aplikasi & checklist miliknya sendiri |
| `SECURITY_REVIEWER` | 2 | kelola `unmatched-queue` & `knowledge-rules` |
| `ADMIN` | 3 | semua akses + kelola users |

Aturan kepemilikan: `DEVELOPER` hanya bisa mengakses aplikasi dengan `owner_id` = dirinya; `ADMIN` bisa semua.

---

## 1. Auth

### POST `/api/auth/login`

Login dengan email + password, mengembalikan JWT. **Publik** (tidak butuh token).

**Headers**

| Header | Nilai |
|--------|-------|
| `Content-Type` | `application/json` |

**Body params**

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `email` | string | ya | format email valid | email user terdaftar |
| `password` | string | ya | tidak boleh kosong | password plaintext |

**Contoh request**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "admin@expertsystem.local", "password": "admin12345" }'
```

**Response `200`** (`LoginResponse`)

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `token` | string | JWT untuk header `Authorization` |
| `tokenType` | string | selalu `"Bearer"` |
| `userId` | number (Long) | id user |
| `name` | string | nama user |
| `role` | string | nama role (`DEVELOPER`/`SECURITY_REVIEWER`/`ADMIN`) |

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Administrator",
  "role": "ADMIN"
}
```

**Kode status**: `200` OK, `400` validasi gagal, `401` email/password salah.

---

## 2. Scan

### POST `/api/scan`

Upload 1 file kode dan scan dengan Semgrep untuk 1 checklist item.

- **Role**: `DEVELOPER`, `ADMIN`
- **Rate limit**: 20 request/menit (kelebihan -> `429`)

**Headers**

| Header | Nilai |
|--------|-------|
| `Authorization` | `Bearer <token>` |
| `Content-Type` | `multipart/form-data` |

**Body params (form-data)**

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `applicationChecklistItemId` | number (Long) | ya | - | id checklist item yang sedang dicek |
| `frameworkId` | number (Integer) | ya | - | menentukan `knowledge_rules` mana yang dipakai untuk matching |
| `file` | file | ya | ekstensi `.java`/`.js`/`.ts`/`.vue`, maks 2 MB | file kode sumber |

**Contoh request**
```bash
curl -X POST http://localhost:8080/api/scan \
  -H "Authorization: Bearer <token>" \
  -F "applicationChecklistItemId=10" \
  -F "frameworkId=1" \
  -F "file=@/path/ke/Kode.java"
```

**Response `200`** (`ScanResponse`)

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `scanStatus` | string | `SUCCESS` \| `FAILED` \| `TIMEOUT` |
| `suggestedStatus` | string | `VALID` \| `INVALID` \| `NA` (NA jika scan gagal/timeout) |
| `findings` | array<Finding> | daftar temuan (lihat di bawah) |

**Finding**

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `semgrepCheckId` | string | id rule Semgrep yang cocok |
| `line` | number | baris temuan |
| `severityRaw` | string | severity mentah dari Semgrep |
| `matchedInKnowledgeBase` | boolean | `true` jika cocok dengan `knowledge_rules` |
| `recommendationText` | string\|null | rekomendasi (null jika unmatched) |
| `referenceUrl` | string\|null | URL referensi (null jika unmatched) |
| `rawMessage` | string\|null | pesan asli Semgrep (terisi jika unmatched) |

```json
{
  "scanStatus": "SUCCESS",
  "suggestedStatus": "INVALID",
  "findings": [
    {
      "semgrepCheckId": "java.lang.security.audit.sqli",
      "line": 42,
      "severityRaw": "ERROR",
      "matchedInKnowledgeBase": true,
      "recommendationText": "Gunakan PreparedStatement...",
      "referenceUrl": "https://owasp.org/...",
      "rawMessage": null
    }
  ]
}
```

**Kode status**: `200` OK, `400` field wajib kosong / file tidak valid, `401`, `403`, `429` rate limit, `500`.

---

## 3. Applications

**Role semua endpoint di bagian ini**: `DEVELOPER`, `ADMIN`.
**Headers**: `Authorization: Bearer <token>` (dan `Content-Type: application/json` untuk POST berbody).

### POST `/api/applications`

Buat aplikasi baru (owner otomatis = user login).

**Body params** (`ApplicationRequest`)

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `name` | string | ya | maks 150 karakter | nama aplikasi |
| `description` | string | tidak | - | deskripsi bebas |

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{ "name": "Aplikasi A", "description": "opsional" }'
```

**Response `201`** (`ApplicationResponse`) - lihat [skema](#applicationresponse).

**Kode status**: `201`, `400`, `401`, `403`.

### GET `/api/applications`

List aplikasi (milik sendiri; `ADMIN` melihat semua).

**Response `200`**: array `ApplicationResponse`.

### GET `/api/applications/{id}`

Detail 1 aplikasi.

**Path params**

| Param | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id aplikasi |

**Response `200`**: `ApplicationResponse`. **Kode status**: `200`, `403` (bukan milik sendiri), `404`.

### POST `/api/applications/{id}/update`

Update aplikasi.

**Path params**: `id` (Long).
**Body params**: sama seperti `ApplicationRequest` (`name` wajib, `description` opsional).
**Response `200`**: `ApplicationResponse`. **Kode status**: `200`, `400`, `403`, `404`.

### POST `/api/applications/{id}/delete`

Hapus aplikasi.

**Path params**: `id` (Long).
**Response `204`** (tanpa body). **Kode status**: `204`, `403`, `404`.

### POST `/api/applications/{id}/checklist/generate`

Generate checklist item dari `ChecklistMaster` aktif. **Idempoten** - item yang sudah ada (per `checklist_master`) tidak diduplikasi.

**Path params**: `id` (Long) - id aplikasi.
**Response `200`**: array `ChecklistItemResponse`. **Kode status**: `200`, `403`, `404`.

### GET `/api/applications/{id}/checklist`

List checklist item milik aplikasi.

**Path params**: `id` (Long) - id aplikasi.
**Response `200`**: array `ChecklistItemResponse`.

### POST `/api/applications/checklist/{itemId}/update`

Update status/keterangan 1 checklist item secara manual.

**Path params**

| Param | Tipe | Deskripsi |
|-------|------|-----------|
| `itemId` | number (Long) | id `application_checklist_item` |

**Body params** (`ChecklistItemUpdateRequest`)

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `status` | string | tidak | salah satu: `NA`, `VALID`, `INVALID` | status baru |
| `keterangan` | string | tidak | - | catatan bebas |

```bash
curl -X POST http://localhost:8080/api/applications/checklist/10/update \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{ "status": "VALID", "keterangan": "sudah diperbaiki" }'
```

**Response `200`**: `ChecklistItemResponse`. **Kode status**: `200`, `400`, `403`, `404`.

---

## 4. Unmatched Queue (Security Reviewer)

**Role**: `SECURITY_REVIEWER`, `ADMIN`. **Headers**: `Authorization: Bearer <token>`.

### GET `/api/unmatched-queue`

List item yang temuannya belum cocok dengan knowledge base.

**Query params**

| Param | Tipe | Wajib | Default | Deskripsi |
|-------|------|:-----:|:-------:|-----------|
| `includeReviewed` | boolean | tidak | `false` | jika `true`, sertakan yang sudah direview |

```bash
curl "http://localhost:8080/api/unmatched-queue?includeReviewed=false" \
  -H "Authorization: Bearer <token>"
```

**Response `200`**: array `UnmatchedQueueResponse` - lihat [skema](#unmatchedqueueresponse).

### POST `/api/unmatched-queue/{id}/review`

Tandai 1 item sebagai sudah direview (`reviewedBy` = user login).

**Path params**: `id` (Long) - id item unmatched_queue.
**Response `204`** (tanpa body). **Kode status**: `204`, `404`.

---

## 5. Knowledge Rules (Security Reviewer)

**Role**: `SECURITY_REVIEWER`, `ADMIN`. **Headers**: `Authorization: Bearer <token>` (+ `Content-Type: application/json` untuk POST berbody).

### GET `/api/knowledge-rules`

List semua rule. **Response `200`**: array `KnowledgeRuleResponse`.

### GET `/api/knowledge-rules/{id}`

Detail 1 rule. **Path params**: `id` (Long). **Response `200`**: `KnowledgeRuleResponse`. `404` jika tidak ada.

### POST `/api/knowledge-rules`

Tambah rule baru.

**Body params** (`KnowledgeRuleRequest`)

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `checklistMasterId` | number (Long) | ya | ada di tabel `checklist_master` | item checklist yang dipetakan |
| `frameworkId` | number (Integer) | ya | - | framework terkait |
| `semgrepCheckId` | string | ya | tidak boleh kosong | id rule Semgrep |
| `recommendationText` | string | ya | tidak boleh kosong | teks rekomendasi |
| `severitySource` | string | tidak | - | sumber/level severity |
| `referenceUrl` | string | tidak | - | URL referensi |

```bash
curl -X POST http://localhost:8080/api/knowledge-rules \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{
    "checklistMasterId": 3,
    "frameworkId": 1,
    "semgrepCheckId": "java.lang.security.audit.sqli",
    "recommendationText": "Gunakan PreparedStatement...",
    "severitySource": "HIGH",
    "referenceUrl": "https://owasp.org/..."
  }'
```

**Response `201`**: `KnowledgeRuleResponse`. **Kode status**: `201`, `400`, `404` (checklistMaster tidak ada).

### POST `/api/knowledge-rules/{id}/update`

Update rule. **Path params**: `id` (Long). **Body**: sama seperti create. **Response `200`**: `KnowledgeRuleResponse`.

### POST `/api/knowledge-rules/{id}/delete`

Hapus rule. **Path params**: `id` (Long). **Response `204`**.

### POST `/api/knowledge-rules/from-unmatched/{queueId}`

Buat rule dari item `unmatched-queue`, sekaligus menandai queue tersebut `reviewed` (dalam 1 transaksi).

**Path params**

| Param | Tipe | Deskripsi |
|-------|------|-----------|
| `queueId` | number (Long) | id item unmatched_queue |

**Body**: sama seperti `KnowledgeRuleRequest`.
**Response `201`**: `KnowledgeRuleResponse`. **Kode status**: `201`, `400`, `404`.

---

## 6. Users (Admin)

**Role**: `ADMIN`. **Headers**: `Authorization: Bearer <token>` (+ `Content-Type: application/json` untuk POST).

### GET `/api/users`

List semua user. **Response `200`**: array `UserResponse`.

### POST `/api/users`

Buat user baru (password otomatis di-hash BCrypt).

**Body params** (`CreateUserRequest`)

| Field | Tipe | Wajib | Constraint | Deskripsi |
|-------|------|:-----:|------------|-----------|
| `name` | string | ya | maks 150 karakter | nama user |
| `email` | string | ya | format email valid, unik | email login |
| `password` | string | ya | minimal 8 karakter | password plaintext (akan di-hash) |
| `roleId` | number (Integer) | ya | `1`=DEVELOPER, `2`=SECURITY_REVIEWER, `3`=ADMIN | role user |

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{ "name": "Budi", "email": "budi@mail.com", "password": "rahasia123", "roleId": 1 }'
```

**Response `201`**: `UserResponse`. **Kode status**: `201`, `400` (email sudah terdaftar/validasi), `403`, `404` (role tidak ada).

---

## 7. Frameworks

**Role**: `DEVELOPER`, `SECURITY_REVIEWER`, `ADMIN` (semua user login). **Headers**: `Authorization: Bearer <token>`.
Data referensi untuk mengisi `frameworkId` di scan & knowledge rules.

### GET `/api/frameworks`

List framework.

**Query params**

| Param | Tipe | Wajib | Default | Deskripsi |
|-------|------|:-----:|:-------:|-----------|
| `includeInactive` | boolean | tidak | `false` | jika `true`, sertakan framework yang non-aktif |

```bash
curl "http://localhost:8080/api/frameworks" -H "Authorization: Bearer <token>"
```

**Response `200`**: array `FrameworkResponse` - lihat [skema](#frameworkresponse).

### GET `/api/frameworks/{id}`

Detail 1 framework.

**Path params**: `id` (Integer).
**Response `200`**: `FrameworkResponse`. **Kode status**: `200`, `404`.

---

## 8. Utility / non-API

| Method | Path | Auth | Deskripsi |
|--------|------|------|-----------|
| GET | `/` | publik | Redirect (303) ke Swagger UI |
| GET | `/swagger-ui` | publik | Swagger UI |
| GET | `/q/openapi` | publik | Spesifikasi OpenAPI (YAML) |
| GET | `/hello` | publik | Endpoint contoh bawaan |

---

## Skema Data (Model)

### ApplicationResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id aplikasi |
| `name` | string | nama |
| `description` | string\|null | deskripsi |
| `ownerId` | number (Long) | id user pemilik |
| `createdAt` | string (ISO-8601) | waktu dibuat |

### ChecklistItemResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id checklist item |
| `applicationId` | number (Long) | id aplikasi |
| `checklistMasterId` | number (Long) | id checklist master |
| `itemCode` | string | kode item (dari master) |
| `itemDescription` | string | deskripsi item (dari master) |
| `severity` | string | severity (dari master) |
| `status` | string | `NA` \| `VALID` \| `INVALID` |
| `keterangan` | string\|null | catatan |
| `updatedBy` | number (Long)\|null | id user yang terakhir update |
| `updatedAt` | string (ISO-8601) | waktu update terakhir |

### UnmatchedQueueResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id item queue |
| `scanResultId` | number (Long) | id scan_result terkait |
| `reviewed` | boolean | sudah direview? |
| `reviewedBy` | number (Long)\|null | id reviewer |
| `semgrepCheckId` | string | id rule Semgrep (dari scan_result) |
| `matchedLine` | number\|null | baris temuan |
| `message` | string\|null | pesan asli Semgrep |
| `severityRaw` | string\|null | severity mentah |

### KnowledgeRuleResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id rule |
| `checklistMasterId` | number (Long) | id checklist master yang dipetakan |
| `frameworkId` | number (Integer) | id framework |
| `semgrepCheckId` | string | id rule Semgrep |
| `recommendationText` | string | teks rekomendasi |
| `severitySource` | string\|null | sumber/level severity |
| `referenceUrl` | string\|null | URL referensi |

### UserResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Long) | id user |
| `name` | string | nama |
| `email` | string | email |
| `role` | string | nama role |
| `createdAt` | string (ISO-8601) | waktu dibuat |

### FrameworkResponse

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | number (Integer) | id framework |
| `name` | string | nama framework |
| `type` | string | `FRONTEND` \| `BACKEND` \| `DATABASE` \| `INFRA` |
| `language` | string | bahasa terkait |
| `isActive` | boolean | status aktif |

---

## Kode Status HTTP

| Kode | Arti |
|:----:|------|
| `200` | OK |
| `201` | Created (resource baru berhasil dibuat) |
| `204` | No Content (berhasil, tanpa body - untuk update/delete) |
| `400` | Bad Request (validasi/field salah) |
| `401` | Unauthorized (belum login / token tidak valid) |
| `403` | Forbidden (role atau kepemilikan tidak sesuai) |
| `404` | Not Found (data tidak ditemukan) |
| `429` | Too Many Requests (rate limit terlampaui) |
| `500` | Internal Server Error |

---

## Catatan

- User ADMIN awal dibuat otomatis saat startup **hanya jika tabel `users` kosong**: `admin@expertsystem.local` / `admin12345`. **Segera ganti password ini.** Nonaktifkan lewat `app.bootstrap-admin.enabled=false`.
- Semua timestamp memakai format ISO-8601 dengan offset (`OffsetDateTime`), mis. `2026-07-12T05:00:00+07:00`.
