# User Stories: Merchant Dashboard (Web)

**Personas:** MERCHANT_OWNER, MERCHANT_STAFF (role-based permissions: products, orders, inventory, pricing, etc.).

**Relevant API prefixes:** `/auth`, `/onboarding`, `/stores/{storeId}/products`, orders, delivery, inventory, pricing, pos, payments.

---

## 2.1 Auth & onboarding

| As a... | I want to... | So that... | API(s) |
| -------- | -------------------------------------------------- | -------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| User | Register with email/password | I can access the platform | `POST /auth/register` |
| User | Login and get tokens | I can use the dashboard | `POST /auth/login`, `POST /auth/refresh` |
| User | Verify email with OTP | My account is verified | `POST /auth/verify-email`, `POST /auth/verify-email/resend` |
| User | Reset password (request + confirm) | I can recover access | `POST /auth/reset-password/request`, `POST /auth/reset-password/confirm` |
| Merchant | Create my business (step 1) | I can onboard my company | `POST /onboarding/businesses` |
| Merchant | Create a store under my business | I can sell from that store | `POST /onboarding/businesses/{id}/stores` |
| Merchant | Upload compliance documents (CAC, TIN, ID) | My business can be verified | `POST /onboarding/businesses/{id}/documents` (multipart) |
| Merchant | Add bank account for a store | I can receive payouts | `POST /onboarding/stores/{id}/bank-accounts` |
| Merchant | View/start subscription (trial, subscribe, verify) | I can use Pro/Enterprise features | `GET /onboarding/businesses/{id}/subscription`, `POST .../start-trial`, `POST .../subscribe`, `POST .../verify` |
| Merchant | Submit business for verification | Admin can approve me | `POST /onboarding/businesses/{id}/submit` |
| Merchant | Activate store after approval | Store goes live | `POST /onboarding/stores/{id}/activate` |
| Merchant | List my stores / get store details | I can switch context and manage stores | `GET /onboarding/me/stores`, `GET /onboarding/stores/{id}` |

---

## 2.2 Business members (owner only)

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------ | --------------------------- | ----------------------------------------------------------------- |
| Owner | List members, invite by email, add existing user | I can grow my team | `GET/POST /onboarding/businesses/{id}/members`, `POST .../invite` |
| Owner | Update member role/status, remove member | I control access | `PATCH/DELETE .../members/{memberPublicId}` |
| Owner | List merchant roles (MANAGER, CASHIER, etc.) | I can assign the right role | `GET /onboarding/roles` |

---

## 2.3 Catalog (products & collections)

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------------------ | --------------------------- | -------------------------------------------------------------------- |
| Merchant | List/get products (with status, search) | I can manage catalog | `GET /stores/{storeId}/products`, `GET .../products/{productId}` |
| Merchant | Create/update/delete product (JSON or multipart with images) | I can add and edit products | `POST/PUT/DELETE /stores/{storeId}/products` |
| Merchant | List/create collections | I can group products | `GET/POST /stores/{storeId}/collections`, `GET .../collections/{id}` |

---

## 2.4 Inventory

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------- | ----------------------------------- | ------------------------------------------------------------------ |
| Merchant | List/create locations | I can represent warehouses/branches | `GET/POST /stores/{storeId}/inventory/locations` |
| Merchant | Create or get inventory item (variant + location) | I can track stock per location | `POST /stores/{storeId}/inventory/items` |
| Merchant | List inventory items, see low stock | I can monitor stock levels | `GET /stores/{storeId}/inventory/items`, `GET .../items/low-stock` |
| Merchant | Adjust stock, reserve, release | I can correct and hold inventory | `POST .../items/{id}/adjust`, `.../reserve`, `.../release` |

---

## 2.5 Pricing & promotions

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------ | ----------------------------------- | ----------------------------------------------------- |
| Merchant | List/create/update price rules | I can run promotions | `GET/POST/PUT /stores/{storeId}/pricing/rules` |
| Merchant | Add discount codes to a rule | Customers can use codes at checkout | `POST /stores/{storeId}/pricing/rules/{ruleId}/codes` |

---

## 2.6 Orders & delivery (store-scoped)

| As a... | I want to... | So that... | API(s) |
| -------- | -------------------------------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Merchant | List orders (paginated, filter by status) | I can process orders | `GET /stores/{storeId}/orders` |
| Merchant | Get order details, update order status | I can fulfill and track | `GET/PATCH /stores/{storeId}/orders/{orderId}` (status) |
| Merchant | Create delivery order for an order | Order can be fulfilled by rider | `POST /stores/{storeId}/delivery/orders` |
| Merchant | List/get delivery orders, assign rider, reschedule | I can manage logistics | `GET /stores/{storeId}/delivery/orders`, `GET .../orders/{id}`, `PATCH .../orders/{id}/assign`, `PATCH .../reschedule` |

---

## 2.7 POS (offline-first)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------------------------------------- | --------------------------------- | ------------------------------------------------------------------------------------ |
| Merchant | Create/list/get POS registers | I can run in-store sales | `POST/GET /stores/{storeId}/pos/registers`, `GET .../registers/{id}` |
| Merchant | Open/close POS session, get current session | Staff can ring sales per register | `POST .../sessions/open`, `POST .../sessions/{id}/close`, `GET .../sessions/current` |
| Merchant | Sync register (offline-first), view cash drawer, record cash movements | I can reconcile and manage cash | `POST .../sync`, `GET .../cash-drawer`, `POST .../cash-movements` |

---

## 2.8 Payments (merchant-initiated)

| As a... | I want to... | So that... | API(s) |
| ---------------------- | ---------------------------------------------------------- | ------------------------------------------- | --------------------------------------------------------------------------- |
| Merchant / Integration | Initiate and verify payment (e.g. after checkout redirect) | Orders can be paid via Paystack/Flutterwave | `POST /stores/{storeId}/payments/initiate`, `POST .../payments/{id}/verify` |

**Note:** Checkout "create order and pay" is used from the **customer app**; the dashboard may show order/payment status only.

---

## Dev handoff

- **Story format:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); include success/error handling and permission/role checks where applicable.
- **API contract:** See OpenAPI/Swagger or backend DTOs.

See also: [USER_STORIES.md](USER_STORIES.md) for the full set of stories across all apps.
