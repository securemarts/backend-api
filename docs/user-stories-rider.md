# User Stories: Riders App (Mobile)

**Personas:** Rider. APIs cover auth, KYC, and the full delivery workflow (assignments, claim, accept/reject, start, location, complete, POD). Optional SSE for real-time updates.

**Relevant API prefixes:** `/rider/auth`, `/rider/kyc`, `/rider/deliveries`.

---

## 4.1 Auth & profile

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------- | ---------------------------------- | ------------------------------------------------------------------------------- |
| Rider | Register (self-onboarding; starts PENDING) | I can join the platform | `POST /rider/auth/register` |
| Rider | Login, refresh, logout | I can use the app securely | `POST /rider/auth/login`, `POST /rider/auth/refresh`, `POST /rider/auth/logout` |
| Rider | Verify email with OTP | My account is verified | `POST /rider/auth/verify-email`, `POST /rider/auth/verify-email/resend` |
| Rider | View my profile and verification status | I know if I can receive deliveries | `GET /rider/kyc/me` |
| Rider | Upload KYC documents (ID, proof of address) | Admin can approve me | `POST /rider/kyc/documents` (multipart), `GET /rider/kyc/documents` |

---

## 4.2 Deliveries (rider workflow)

| As a... | I want to... | So that... | API(s) |
| ------- | -------------------------------------------------------- | --------------------------------------------- | ------------------------------------------------------------------------------- |
| Rider | See deliveries assigned to me | I know what to deliver | `GET /rider/deliveries` |
| Rider | See available deliveries to claim (when auto-assign off) | I can pick jobs in my area | `GET /rider/deliveries/available` (optional lat, lng, radiusKm) |
| Rider | Claim a PENDING delivery | It gets assigned to me | `POST /rider/deliveries/{id}/claim` |
| Rider | Get real-time delivery events (assigned, available) | I see new jobs without polling | `GET /rider/deliveries/stream` (SSE) |
| Rider | Update my current location | Dispatch can assign me and customer can track | `PATCH /rider/deliveries/me/location` |
| Rider | View delivery details | I know pickup/dropoff and order info | `GET /rider/deliveries/{id}` |
| Rider | Accept or reject an assigned delivery | I can manage my workload | `POST /rider/deliveries/{id}/accept`, `POST .../reject` |
| Rider | Start delivery (picked up / in transit) | Status updates for merchant and customer | `POST /rider/deliveries/{id}/start` (pickedUp flag) |
| Rider | Update location during delivery (tracking) | Customer sees live tracking | `POST /rider/deliveries/{id}/location` |
| Rider | Complete delivery (optional POD inline) | Order is marked delivered | `POST /rider/deliveries/{id}/complete` |
| Rider | Upload proof of delivery (signature/photo) | Merchant has proof | `POST /rider/deliveries/{id}/pod` (multipart), `GET .../pod/{filename}` to view |

---

## Dev handoff

- **Story format:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); include success/error handling. For real-time updates, integrate SSE `GET /rider/deliveries/stream`.
- **API contract:** See OpenAPI/Swagger or backend DTOs.

See also: [USER_STORIES.md](USER_STORIES.md) for the full set of stories across all apps.
