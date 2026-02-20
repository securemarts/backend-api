# User Stories for Securemarts (from Backend APIs)

User stories below are derived from the backend controllers and mapped to each app surface. Each story references the relevant API path(s) so frontend/mobile devs know what to integrate.

**Per-app handoff (for devs):** [Landing](user-stories-landing.md) · [Merchant Dashboard](user-stories-merchant.md) · [Securemarts Users (Customer) App](user-stories-customer.md) · [Riders App](user-stories-rider.md) · [Platform Admin](user-stories-admin.md)

---

## 1. Landing Page (Public, no auth)

**APIs:** `GET /public/subscription-plans`, `GET /storefront/{storeSlug}` (optional for "view a store" CTA), `/auth/*` (link to sign up).

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------------------------------ | -------------------------------------------- | ---------------------------------- |
| Visitor | See subscription plans (Basic, Pro, Enterprise) with limits and features | I can compare plans before signing up | `GET /public/subscription-plans` |
| Visitor | Navigate to "Sign up" / "Login" | I can become a merchant or customer | Link to auth flows using `/auth/*` |
| Visitor | See a CTA to "View sample store" (optional) | I can preview a storefront before committing | `GET /storefront/{storeSlug}` |

**Out of scope for landing:** Discovery and storefront browsing are better suited to the **Securemarts Users App**; landing can link to that app or to merchant signup.

---

## 2. Merchant Dashboard (Web)

**Personas:** MERCHANT_OWNER, MERCHANT_STAFF (role-based permissions: products, orders, inventory, pricing, etc.).

### 2.1 Auth & onboarding

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

### 2.2 Business members (owner only)

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------ | --------------------------- | ----------------------------------------------------------------- |
| Owner | List members, invite by email, add existing user | I can grow my team | `GET/POST /onboarding/businesses/{id}/members`, `POST .../invite` |
| Owner | Update member role/status, remove member | I control access | `PATCH/DELETE .../members/{memberPublicId}` |
| Owner | List merchant roles (MANAGER, CASHIER, etc.) | I can assign the right role | `GET /onboarding/roles` |

### 2.3 Catalog (products & collections)

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------------------ | --------------------------- | -------------------------------------------------------------------- |
| Merchant | List/get products (with status, search) | I can manage catalog | `GET /stores/{storeId}/products`, `GET .../products/{productId}` |
| Merchant | Create/update/delete product (JSON or multipart with images) | I can add and edit products | `POST/PUT/DELETE /stores/{storeId}/products` |
| Merchant | List/create collections | I can group products | `GET/POST /stores/{storeId}/collections`, `GET .../collections/{id}` |

### 2.4 Inventory

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------- | ----------------------------------- | ------------------------------------------------------------------ |
| Merchant | List/create locations | I can represent warehouses/branches | `GET/POST /stores/{storeId}/inventory/locations` |
| Merchant | Create or get inventory item (variant + location) | I can track stock per location | `POST /stores/{storeId}/inventory/items` |
| Merchant | List inventory items, see low stock | I can monitor stock levels | `GET /stores/{storeId}/inventory/items`, `GET .../items/low-stock` |
| Merchant | Adjust stock, reserve, release | I can correct and hold inventory | `POST .../items/{id}/adjust`, `.../reserve`, `.../release` |

### 2.5 Pricing & promotions

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------ | ----------------------------------- | ----------------------------------------------------- |
| Merchant | List/create/update price rules | I can run promotions | `GET/POST/PUT /stores/{storeId}/pricing/rules` |
| Merchant | Add discount codes to a rule | Customers can use codes at checkout | `POST /stores/{storeId}/pricing/rules/{ruleId}/codes` |

### 2.6 Orders & delivery (store-scoped)

| As a... | I want to... | So that... | API(s) |
| -------- | -------------------------------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Merchant | List orders (paginated, filter by status) | I can process orders | `GET /stores/{storeId}/orders` |
| Merchant | Get order details, update order status | I can fulfill and track | `GET/PATCH /stores/{storeId}/orders/{orderId}` (status) |
| Merchant | Create delivery order for an order | Order can be fulfilled by rider | `POST /stores/{storeId}/delivery/orders` |
| Merchant | List/get delivery orders, assign rider, reschedule | I can manage logistics | `GET /stores/{storeId}/delivery/orders`, `GET .../orders/{id}`, `PATCH .../orders/{id}/assign`, `PATCH .../reschedule` |

### 2.7 POS (offline-first)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------------------------------------- | --------------------------------- | ------------------------------------------------------------------------------------ |
| Merchant | Create/list/get POS registers | I can run in-store sales | `POST/GET /stores/{storeId}/pos/registers`, `GET .../registers/{id}` |
| Merchant | Open/close POS session, get current session | Staff can ring sales per register | `POST .../sessions/open`, `POST .../sessions/{id}/close`, `GET .../sessions/current` |
| Merchant | Sync register (offline-first), view cash drawer, record cash movements | I can reconcile and manage cash | `POST .../sync`, `GET .../cash-drawer`, `POST .../cash-movements` |

### 2.8 Payments (merchant-initiated)

| As a... | I want to... | So that... | API(s) |
| ---------------------- | ---------------------------------------------------------- | ------------------------------------------- | --------------------------------------------------------------------------- |
| Merchant / Integration | Initiate and verify payment (e.g. after checkout redirect) | Orders can be paid via Paystack/Flutterwave | `POST /stores/{storeId}/payments/initiate`, `POST .../payments/{id}/verify` |

*Note: Checkout "create order and pay" is used from the **customer app**; dashboard may show order/payment status only.*

---

## 3. Securemarts Users App (Customers / APP_CLIENT)

**APIs:** Storefront (public), Discovery, Cart, Checkout, Payment verify. Auth used for optional "my account" and for checkout if you require login.

### 3.1 Discovery & storefront (browse without login)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------------------------------- | ------------------------------------ | ---------------------------------------------------------------------- |
| Customer | Search stores by name, city/state, or location (lat/lng, radius) | I can find stores that deliver to me | `GET /discovery/stores` (q, city, state, lat, lng, radiusKm) |
| Customer | See store locations (pick-up points) for a store | I can choose branch/area | `GET /discovery/stores/{storeId}/locations` |
| Customer | Check inventory availability at a location | I know what's in stock where | `GET /discovery/stores/{storeId}/locations/{locId}/availability` |
| Customer | See delivery ETA for my address | I know when to expect delivery | `GET /discovery/stores/{storeId}/delivery-eta` (lat/lng or city/state) |
| Customer | View a store by slug (name/URL) | I can browse a specific store | `GET /storefront/{storeSlug}` |
| Customer | List and search products, view product detail | I can choose what to buy | `GET /storefront/{storeSlug}/products`, `GET .../products/{productId}` |

### 3.2 Cart (store-scoped; cart token in header)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------- | ------------------------------- | ------------------------------------------------------------- |
| Customer | Get or create cart (by token or cart ID) | I can add items without account | `GET /stores/{storeId}/cart` (X-Cart-Token or cartId) |
| Customer | Add item to cart | I can build my order | `POST /stores/{storeId}/cart/items` |
| Customer | Update quantity or remove item | I can correct my cart | `PATCH/DELETE /stores/{storeId}/cart/{cartId}/items/{itemId}` |

### 3.3 Checkout & payment

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------------ | ---------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Customer | Create order from cart (no payment) | I get an order to pay later or in-person | `POST /stores/{storeId}/checkout/create-order` (cartId) |
| Customer | Create order and pay in one step (redirect to gateway) | I can pay online and get confirmation | `POST /stores/{storeId}/checkout/create-order-and-pay` (cartId, email, callbackUrl, deliveryAddress, etc.) |
| Customer | Apply discount code at checkout | I get the discounted total | `POST /stores/{storeId}/pricing/apply` (code, subtotal) |
| Customer | Verify payment after return from gateway | My order is confirmed paid | `POST /stores/{storeId}/payments/{paymentId}/verify` |

### 3.4 Auth (optional for "my account" or saved addresses)

| As a... | I want to... | So that... | API(s) |
| -------- | ----------------------------------- | ----------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| Customer | Register / login / refresh / logout | I can have an account and faster checkout | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` |
| Customer | Verify email, reset password | My account is secure and recoverable | `POST /auth/verify-email`, `.../verify-email/resend`, `.../reset-password/request`, `.../reset-password/confirm` |

*Note: Backend supports CUSTOMER and MERCHANT_OWNER/MERCHANT_STAFF via same `/auth`; app should use registration flow that sets user type or context (e.g. "Sign up as customer" vs "Sign up as merchant" linking to dashboard).*

---

## 4. Riders App (Mobile)

**APIs:** Rider auth, KYC, deliveries (assignments, claim, accept/reject, start, location, complete, POD), optional SSE for real-time updates.

### 4.1 Auth & profile

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------- | ---------------------------------- | ------------------------------------------------------------------------------- |
| Rider | Register (self-onboarding; starts PENDING) | I can join the platform | `POST /rider/auth/register` |
| Rider | Login, refresh, logout | I can use the app securely | `POST /rider/auth/login`, `POST /rider/auth/refresh`, `POST /rider/auth/logout` |
| Rider | Verify email with OTP | My account is verified | `POST /rider/auth/verify-email`, `POST /rider/auth/verify-email/resend` |
| Rider | View my profile and verification status | I know if I can receive deliveries | `GET /rider/kyc/me` |
| Rider | Upload KYC documents (ID, proof of address) | Admin can approve me | `POST /rider/kyc/documents` (multipart), `GET /rider/kyc/documents` |

### 4.2 Deliveries (rider workflow)

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

## 5. Platform Admin (separate dashboard; backend only)

*If you have an admin dashboard, these map to the admin APIs.*

- **Auth:** `POST /admin/auth/login`, `GET /admin/auth/me`, create/invite admin, `POST /admin/auth/complete-setup`.
- **Businesses:** List businesses, approve/reject verification, update subscription — `AdminBusinessManagementController`.
- **Logistics:** Service zones CRUD, assign store to zone, list/get/update riders, approve/reject rider KYC — `AdminLogisticsController`.
- **RBAC:** Admins, roles, permissions, merchant roles, merchant permissions — `AdminUserManagementController`, `AdminRoleManagementController`, `AdminPermissionManagementController`, `AdminMerchantRoleController`, `AdminMerchantPermissionController`.

---

## Summary by app

| App | Main API prefixes | Notes |
| ------------------------- | ---------------------------------------------------- | -------------------------------------------------------------- |
| **Landing** | `/public`, `/auth` (links) | Plans + sign up / login entry points. |
| **Merchant Dashboard** | `/auth`, `/onboarding`, `/stores/{storeId}/products`, orders, delivery, inventory, pricing, pos, payments | Full merchant lifecycle and store operations; permission-aware. |
| **Securemarts Users App** | `/storefront`, `/discovery`, `/stores/{storeId}/cart`, checkout, payments | Browse, cart, checkout, pay; auth optional. |
| **Riders App** | `/rider/auth`, `/rider/kyc`, `/rider/deliveries` | Rider lifecycle, KYC, delivery workflow, SSE for live updates. |

---

## Suggested format for dev handoff

- **Story:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); include success/error handling and (where applicable) permission/role checks.
- **API contract:** Method + path + key request/response fields (from OpenAPI/Swagger or DTOs in the backend).

For per-app handoff, see also: `user-stories-landing.md`, `user-stories-merchant.md`, `user-stories-customer.md`, `user-stories-rider.md`, `user-stories-admin.md`.
