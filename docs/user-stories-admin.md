# User Stories: Platform Admin (Backend / Admin Dashboard)

If you have a separate admin dashboard, these map to the admin APIs. All endpoints are under `/admin` and require platform admin authentication (SUPERUSER, PLATFORM_ADMIN, or SUPPORT roles and scopes).

**Relevant API prefixes:** `/admin/auth`, `/admin/businesses`, `/admin/logistics`, `/admin/admins`, `/admin/roles`, `/admin/permissions`, `/admin/merchant-roles`, `/admin/merchant-permissions`.

---

## Auth

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------- | ---------------------------------- | ------------------------------------------------------------------------------- |
| Admin | Login to the admin dashboard | I can manage the platform | `POST /admin/auth/login` |
| Admin | Get my identity, roles, and permission scopes | The UI can show/hide features | `GET /admin/auth/me` |
| Superuser | Create an admin (with password) | New admins can access the dashboard | `POST /admin/auth/admins` |
| Superuser | Invite an admin by email | Invitee completes setup with token + password | `POST /admin/auth/admins/invite` |
| Invitee | Complete setup with invite token and password | I can log in as admin | `POST /admin/auth/complete-setup` (no auth) |

---

## Business management

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------ | --------------------------- | ----------------------------------------------------------------- |
| Admin | List businesses (optional filter by verification status) | I can review pending businesses | `GET /admin/businesses` (status: PENDING, UNDER_REVIEW, APPROVED, REJECTED) |
| Admin | Approve or reject a business (verification) | Merchants can go live or get feedback | `PATCH /admin/businesses/{businessPublicId}/verification` |
| Admin | Update a business subscription (plan, status, trial, period) | I can grant trials or fix subscription issues | `PATCH /admin/businesses/{businessPublicId}/subscription` |

---

## Logistics (service zones & riders)

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------ | --------------------------- | ----------------------------------------------------------------- |
| Admin | List/create/get/update service zones | I can define delivery zones (e.g. by city) | `GET/POST /admin/logistics/service-zones`, `GET/PATCH .../service-zones/{zonePublicId}` |
| Admin | Assign a store to a service zone | The store can create delivery orders in that zone | `PATCH /admin/logistics/stores/{storePublicId}/service-zone` |
| Admin | List riders (optional filter by status, zone) | I can manage riders | `GET /admin/logistics/riders` |
| Admin | Get or update a rider | I can change rider details or zone | `GET/PATCH /admin/logistics/riders/{riderPublicId}` |
| Admin | Approve or reject rider KYC | Riders can receive deliveries when approved | `PATCH /admin/logistics/riders/{riderPublicId}/verification` |

---

## RBAC (admins, roles, permissions)

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------ | --------------------------- | ----------------------------------------------------------------- |
| Superuser | List/get/create/update/delete admins | I can manage admin users | `GET/GET/POST/PATCH/DELETE /admin/admins`, `.../admins/{adminPublicId}` |
| Admin | List/get/create/update/delete platform roles | I can define admin roles | `GET/GET/POST/PATCH/DELETE /admin/roles`, `.../roles/{rolePublicId}` |
| Admin | List/get/create/update/delete permissions | I can define scopes (e.g. business:list) | `GET/GET/POST/PATCH/DELETE /admin/permissions`, `.../permissions/{permissionPublicId}` |
| Admin | Get/put role permissions | I can attach permissions to roles | `GET/PUT /admin/roles/{rolePublicId}/permissions` |
| Admin | List/get/create/update/delete merchant roles | I can define roles for merchant staff | `GET/GET/POST/PATCH/DELETE /admin/merchant-roles`, `.../merchant-roles/{rolePublicId}` |
| Admin | Get/put merchant role permissions | I can attach permissions to merchant roles | `GET/PUT /admin/merchant-roles/{rolePublicId}/permissions` |
| Admin | List/get/create/update/delete merchant permissions | I can define merchant scopes (e.g. orders:read) | `GET/GET/POST/PATCH/DELETE /admin/merchant-permissions`, `.../merchant-permissions/{permissionPublicId}` |

---

## Dev handoff

- **Story format:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); enforce role/scope checks (PreAuthorize on backend).
- **API contract:** See OpenAPI/Swagger or backend DTOs.

See also: [USER_STORIES.md](USER_STORIES.md) for the full set of stories across all apps.
