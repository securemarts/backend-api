# User Stories: Securemarts Users App (Customers)

**Personas:** Customer / APP_CLIENT. Auth is optional for "my account" and for checkout if login is required.

**Relevant API prefixes:** `/storefront`, `/discovery`, `/stores/{storeId}/cart`, checkout, payments, `/auth`.

---

## 3.1 Discovery & storefront (browse without login)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------------------------------- | ------------------------------------ | ---------------------------------------------------------------------- |
| Customer | Search stores by name, city/state, or location (lat/lng, radius) | I can find stores that deliver to me | `GET /discovery/stores` (q, city, state, lat, lng, radiusKm) |
| Customer | See store locations (pick-up points) for a store | I can choose branch/area | `GET /discovery/stores/{storeId}/locations` |
| Customer | Check inventory availability at a location | I know what's in stock where | `GET /discovery/stores/{storeId}/locations/{locId}/availability` |
| Customer | See delivery ETA for my address | I know when to expect delivery | `GET /discovery/stores/{storeId}/delivery-eta` (lat/lng or city/state) |
| Customer | View a store by slug (name/URL) | I can browse a specific store | `GET /storefront/{storeSlug}` |
| Customer | List and search products, view product detail | I can choose what to buy | `GET /storefront/{storeSlug}/products`, `GET .../products/{productId}` |

---

## 3.2 Cart (store-scoped; cart token in header)

| As a... | I want to... | So that... | API(s) |
| -------- | ---------------------------------------- | ------------------------------- | ------------------------------------------------------------- |
| Customer | Get or create cart (by token or cart ID) | I can add items without account | `GET /stores/{storeId}/cart` (X-Cart-Token or cartId) |
| Customer | Add item to cart | I can build my order | `POST /stores/{storeId}/cart/items` |
| Customer | Update quantity or remove item | I can correct my cart | `PATCH/DELETE /stores/{storeId}/cart/{cartId}/items/{itemId}` |

---

## 3.3 Checkout & payment

| As a... | I want to... | So that... | API(s) |
| -------- | ------------------------------------------------------ | ---------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Customer | Create order from cart (no payment) | I get an order to pay later or in-person | `POST /stores/{storeId}/checkout/create-order` (cartId) |
| Customer | Create order and pay in one step (redirect to gateway) | I can pay online and get confirmation | `POST /stores/{storeId}/checkout/create-order-and-pay` (cartId, email, callbackUrl, deliveryAddress, etc.) |
| Customer | Apply discount code at checkout | I get the discounted total | `POST /stores/{storeId}/pricing/apply` (code, subtotal) |
| Customer | Verify payment after return from gateway | My order is confirmed paid | `POST /stores/{storeId}/payments/{paymentId}/verify` |

---

## 3.4 Auth (optional for "my account" or saved addresses)

| As a... | I want to... | So that... | API(s) |
| -------- | ----------------------------------- | ----------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| Customer | Register / login / refresh / logout | I can have an account and faster checkout | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` |
| Customer | Verify email, reset password | My account is secure and recoverable | `POST /auth/verify-email`, `.../verify-email/resend`, `.../reset-password/request`, `.../reset-password/confirm` |

**Note:** The backend supports CUSTOMER and MERCHANT_OWNER/MERCHANT_STAFF via the same `/auth`; the app should use a registration flow that sets user type or context (e.g. "Sign up as customer" vs "Sign up as merchant" linking to the dashboard).

---

## Dev handoff

- **Story format:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); include success/error handling. Cart uses `X-Cart-Token` header where applicable.
- **API contract:** See OpenAPI/Swagger or backend DTOs.

See also: [USER_STORIES.md](USER_STORIES.md) for the full set of stories across all apps.
