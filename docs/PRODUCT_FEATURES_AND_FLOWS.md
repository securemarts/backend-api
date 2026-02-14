# Shopper – Product Features & Flows

**Purpose:** A single source of truth for frontend and mobile engineers to build UIs without Figma. This document maps every feature, how it connects to others, and how it is meant to work.

---

## 1. Product overview

**Shopper** is a multi-tenant commerce and delivery platform (Chowdeck/UberEats style):

- **Merchants** onboard a business and stores, manage catalog and inventory, receive and fulfill orders, and optionally use delivery (riders) and POS.
- **Shoppers** discover stores (by search, city, or location), browse products, add to cart, pay (Paystack/Flutterwave), and can request delivery to an address.
- **Riders** are onboarded by the platform, operate in **service zones**, report location, and get **nearest-rider** assignment when a paid order needs delivery.
- **Platform admins** manage businesses (verification), users, roles, and logistics (service zones, riders, store–zone assignment).

Everything is **store-scoped**: cart, checkout, orders, delivery, and POS use `storePublicId` (or store slug) in the path. Discovery and storefront are **public** (no auth). Merchant and rider areas require **auth** and **role-based access**.

---

## 2. User roles and access

| Role | Who | Main entrypoints |
|------|-----|-------------------|
| **Customer / Shopper** | End buyer | Discovery, storefront, cart, checkout (cart/checkout can be anonymous via cart token). |
| **MERCHANT_OWNER / MERCHANT_STAFF** | Store staff | Onboarding (business/store), catalog, inventory, orders, delivery, pricing, POS. Access is per-store and permission-based (e.g. `orders:read`, `orders:write`). |
| **RIDER** | Delivery rider | Rider auth; “my deliveries”; update location; accept/reject, start, complete delivery; POD. |
| **PLATFORM_ADMIN / SUPERUSER / SUPPORT** | Platform ops | Admin auth; user/business management; verification; admin logistics (service zones, riders, store–zone). |

**Auth:**

- **Merchants/Customers:** `POST /auth/register`, `POST /auth/login`. Response: `accessToken`, `refreshToken`, user info. Use `Authorization: Bearer <accessToken>`.
- **Riders:** `POST /rider/auth/login` (separate JWT).
- **Admins:** `POST /admin/auth/login`, optional `POST /admin/auth/complete-setup` for first-time setup.

**Public (no auth):**

- `GET /storefront/**`, `GET /discovery/**`, `GET /stores/{storePublicId}/cart/**`, `POST /stores/{storePublicId}/pricing/apply`, `GET /stores/{storePublicId}/uploads/**`.

---

## 3. How the system fits together (high level)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SHOPPER PLATFORM                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  DISCOVERY (public)          STOREFRONT (public)        CART (public token)  │
│  Search stores by q/city/    Store by slug, products    Get/create cart,     │
│  lat/lng, list locations,    list/detail                add/update items     │
│  availability, delivery ETA                                                │
│         │                             │                          │           │
│         └─────────────────────────────┴──────────────────────────┘           │
│                                    │                                          │
│  CHECKOUT (auth optional)         ▼                                          │
│  Create order from cart  ──►  CREATE ORDER + INITIATE PAYMENT                 │
│  Create order + pay (one call)     │                                          │
│                                    ▼                                          │
│  PAYMENT (redirect to gateway)  ◄──►  Callback / verify payment               │
│                                    │                                          │
│  ORDER LIFECYCLE (merchant)        │                                          │
│  List/get/update order status      │                                          │
│  (PENDING → CONFIRMED → PAID → PROCESSING → SHIPPED → DELIVERED)              │
│                                    │                                          │
│  DELIVERY (Chowdeck model)         │                                          │
│  When order is paid & delivery     │                                          │
│  requested: create delivery order  │                                          │
│  (customer lat/lng required)  ──►  Zone check → Fee calc → Assign nearest rider│
│  List deliveries, assign rider,    │                                          │
│  reschedule failed/returned       │                                          │
│                                    │                                          │
│  RIDER APP                         │                                          │
│  My deliveries, update my          │                                          │
│  location, accept/reject,          │                                          │
│  start → in-transit → complete, POD │                                          │
└─────────────────────────────────────────────────────────────────────────────┘

  ADMIN (separate auth)
  Businesses, verification, users, roles, permissions, service zones, riders,
  assign store → service zone.
```

---

## 4. Feature map and flows

### 4.1 Authentication (merchant/customer)

| Feature | Description | API / behaviour |
|--------|-------------|------------------|
| Register | New user (merchant or customer) | `POST /auth/register` → tokens + user. |
| Login | Email/password | `POST /auth/login` → `accessToken`, `refreshToken`. |
| Refresh | Before access token expires | `POST /auth/refresh` with `refreshToken` → new pair. |
| Logout | Invalidate refresh token | `POST /auth/logout` (body: `refreshToken`). |
| Verify email | Link from email | `POST /auth/verify-email` with token. |
| Verify phone | OTP (stub) | `POST /auth/verify-phone`. |
| Reset password | Request link | `POST /auth/reset-password/request`. Confirm with token: `POST /auth/reset-password/confirm`. |

**UI notes:** After login/register, store `accessToken` and `refreshToken`. Use Bearer token on all authenticated requests. Implement refresh (e.g. on 401 or before expiry) and optional logout that sends refresh token to backend.

---

### 4.2 Discovery (public – no auth)

Used by shoppers to find stores and see what’s available where.

| Feature | Description | API | Response / usage |
|--------|-------------|-----|-------------------|
| Search stores | By name/brand, city/state, or geo | `GET /discovery/stores?q=&state=&city=&lat=&lng=&radiusKm=&includeLocations=&page=&size=` | Paginated list. With `lat`/`lng`, results include `distanceKm` and can be sorted by distance. Use `includeLocations=true` to get locations per store for “choose branch”. |
| List locations for store | Branches/pick-up points | `GET /discovery/stores/{storePublicId}/locations` | List of locations (publicId, name, address, etc.). Use `locationPublicId` in availability. |
| Availability at location | What’s in stock at a location (for menu/product list) | `GET /discovery/stores/{storePublicId}/locations/{locationPublicId}/availability?variantIds=` | Variants with quantity, product title, variant title, price, image. Optional `variantIds` to restrict. |
| Delivery ETA | Rough ETA for store → customer | `GET /discovery/stores/{storePublicId}/delivery-eta?lat=&lng=&city=&state=` | `estimatedHours`, `zoneOrHubName` (service zone name). |

**Flow (typical):**

1. **Home / search:** Call `GET /discovery/stores` with `q` or `city`/`state` or `lat`/`lng` (+ optional `radiusKm`). Show list; show `distanceKm` when geo provided.
2. **Store → locations:** User picks a store → `GET /discovery/stores/{storePublicId}/locations` to show branches.
3. **Menu / products at location:** Pick a location → `GET .../locations/{locationPublicId}/availability` to build menu with prices and stock.
4. **Delivery ETA (optional):** Before checkout, call `delivery-eta` with customer lat/lng or city/state to show “Delivery in ~X hours”.

**Connections:** Discovery uses **store publicId** and **location publicId**. Cart and storefront use the same store (by slug or publicId). Availability drives what can be added to cart (by variant).

---

### 4.3 Storefront (public – no auth)

Storefront is by **store slug** (URL-friendly name), not publicId.

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| Get store by slug | Public store info | `GET /storefront/{storeSlug}` | Store name, slug, publicId (use publicId for cart/checkout). |
| List products | Paginated, optional search | `GET /storefront/{storeSlug}/products?q=&page=&size=` | Product list for store. |
| Get product | Single product detail | `GET /storefront/{storeSlug}/products/{productPublicId}` | Product with variants (for PDP). |

**Connection:** Store slug is for sharing (e.g. `yoursite.com/store/coffee-shop`). Cart and checkout use **store publicId** from the store object. Discovery can return the same store (by publicId); storefront is the “shop this store” entry by slug.

---

### 4.4 Cart (public – cart token for anonymous)

Cart is **per store**. Anonymous users get a cart token in the response; send it back so the same cart is used.

| Feature | Description | API | Request / response |
|--------|-------------|-----|--------------------|
| Get or create cart | By token or cart ID | `GET /stores/{storePublicId}/cart?cartId=` + optional header `X-Cart-Token` | Returns cart with items, totals. Response may include `cartToken` – store it (e.g. localStorage) and send as `X-Cart-Token` next time. |
| Add item | Variant + quantity | `POST /stores/{storePublicId}/cart/items` + optional `X-Cart-Token`, body: `variantPublicId`, `quantity` | Returns updated cart. |
| Update quantity | Change or remove (0) | `PATCH /stores/{storePublicId}/cart/{cartPublicId}/items/{cartItemPublicId}` body: `{"quantity": 2}` | Updated cart. |
| Remove item | Delete line | `DELETE /stores/{storePublicId}/cart/{cartPublicId}/items/{cartItemPublicId}` | Updated cart. |

**Flow:** From discovery availability or storefront product, use `variantPublicId` to add to cart. Always send `X-Cart-Token` if you have it so multiple devices/tabs share the same cart. Cart is keyed by store; switching store = different cart.

**Connections:** Cart items reference **variants**. At checkout you pass `cartId` (cart publicId). Subtotal from cart can be used with pricing apply (discount code).

---

### 4.5 Pricing (discount codes)

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| Apply discount (public) | Get discounted total for checkout | `POST /stores/{storePublicId}/pricing/apply` body: `{"code":"SAVE10","subtotal":"5000"}` | Returns `subtotal`, `discountedTotal`, `code`. Use `discountedTotal` as amount to charge. |
| Price rules & codes (merchant) | CRUD rules, add codes | `GET/POST /stores/{storePublicId}/pricing/rules`, `POST .../rules/{id}/codes` (auth) | Merchant creates rules and codes; shopper only uses apply. |

**Flow:** On checkout screen, user may enter a code. Call `pricing/apply` with cart subtotal and code; show discounted total and optionally “Pay ₦X” (discountedTotal).

---

### 4.6 Checkout and payment

| Feature | Description | API | Request / response |
|--------|-------------|-----|--------------------|
| Create order only | Convert cart to order, clear cart | `POST /stores/{storePublicId}/checkout/create-order?cartId=` (auth) | Returns `OrderResponse`. Use when you want to pay later or use a different payment path. |
| Create order and pay (one call) | Create order + initiate payment in one step | `POST /stores/{storePublicId}/checkout/create-order-and-pay` (auth) body: `cartId`, `email`, `callbackUrl`, `gateway` (e.g. PAYSTACK, FLUTTERWAVE) | Returns `order` + `payment`. **Redirect user to `payment.authorizationUrl`** to complete payment on gateway. |
| Verify payment | After user returns from gateway | `POST /stores/{storePublicId}/payments/{paymentPublicId}/verify` | Returns payment status. On success, order is PAID. |

**Flow:**

1. User has cart; optionally applied discount (use `discountedTotal` as amount).
2. Call `create-order-and-pay` with cartId, email, callbackUrl, gateway.
3. Redirect to `payment.authorizationUrl`.
4. On return (callback URL), call `payments/{paymentPublicId}/verify` (paymentPublicId can come from callback query or state).
5. If verified success → show “Order confirmed” and order details; order status is PAID. Optionally prompt “Request delivery” (see Delivery).

**Connections:** Order is created from cart; payment is linked to that order. Order has `orderPublicId` and `status`. Delivery creation needs `orderPublicId` and customer `deliveryLat`/`deliveryLng`.

---

### 4.7 Orders (merchant)

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| List orders | Paginated, optional status filter | `GET /stores/{storePublicId}/orders?status=&page=&size=` | Merchant dashboard. |
| Get order | Single order | `GET /stores/{storePublicId}/orders/{orderPublicId}` | Order detail, items, total, status. |
| Update status | Move order along lifecycle | `PATCH /stores/{storePublicId}/orders/{orderPublicId}/status` body: `{"status":"CONFIRMED"}` | Statuses: PENDING, CONFIRMED, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED. |

**Flow:** After payment, order is PAID. Merchant can set to PROCESSING, then SHIPPED/DELIVERED (or use delivery flow). If delivery is requested, merchant (or system) creates a delivery order (see below).

---

### 4.8 Delivery (Chowdeck-style)

Delivery is **store-scoped** and uses **service zones**, **distance-based fee**, and **nearest available rider** in zone.

**Preconditions:**

- Store has a **service zone** assigned (admin: `PATCH /admin/logistics/stores/{storePublicId}/service-zone`).
- Store profile has **latitude/longitude** (store location for fee and rider distance).
- At least one **rider** in that zone, **available**, with **current_lat/current_lng** set.

| Feature | Description | API | Request / response |
|--------|-------------|-----|--------------------|
| Create delivery order | After order is paid; auto-assigns nearest rider in zone | `POST /stores/{storePublicId}/delivery/orders` (auth, orders:write) body: `orderPublicId`, `deliveryAddress`, **`deliveryLat`**, **`deliveryLng`**, optional `pickupAddress`, `scheduledAt` | Backend: 1) Checks customer inside zone, 2) Computes fee (base + per_km × distance), 3) Finds nearest available rider, 4) Assigns rider and sets delivery status ASSIGNED. Returns delivery order with rider, fee, status. |
| List delivery orders | By store, optional status | `GET /stores/{storePublicId}/delivery/orders?status=` | For merchant delivery list. |
| Get delivery order | Single delivery | `GET /stores/{storePublicId}/delivery/orders/{deliveryOrderPublicId}` | Detail view. |
| Assign rider (manual) | Override / assign if auto didn’t run | `PATCH /stores/{storePublicId}/delivery/orders/{deliveryOrderPublicId}/assign` body: `riderPublicId` | Only for PENDING. Rider must be available. |
| Reschedule | Re-attempt after FAILED/RETURNED | `PATCH /stores/{storePublicId}/delivery/orders/{deliveryOrderPublicId}/reschedule` body: optional `scheduledAt` | Status back to PENDING, rider cleared. |

**Delivery statuses:** PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED (or FAILED, RETURNED).

**Flow (merchant):**

1. Order is PAID; customer wants delivery.
2. Merchant (or app) collects delivery address and **customer lat/lng** (required).
3. Call `POST .../delivery/orders` with `orderPublicId`, `deliveryAddress`, `deliveryLat`, `deliveryLng`.
4. Backend validates zone, computes fee, assigns nearest rider. If no rider or out of zone, API returns error.
5. Merchant sees delivery in list with rider and status; rider sees it in Rider app.

**Connections:** Delivery order is tied to **order** (orderPublicId). Store must be in a **service zone**; rider must be in same zone and **available**. Fee is stored on delivery order (`pricingAmount`, `pricingCurrency`).

---

### 4.9 Rider app

Riders have **separate auth** (`/rider/auth/login`) and **RIDER** role.

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| Update my location | Set rider’s current position (for dispatch) | `PATCH /rider/deliveries/me/location` body: `latitude`, `longitude` | Call when going “available” and periodically so nearest-rider assignment works. |
| My deliveries | List assigned/active | `GET /rider/deliveries` | ASSIGNED, PICKED_UP, IN_TRANSIT. |
| Get delivery | One delivery detail | `GET /rider/deliveries/{deliveryOrderPublicId}` | |
| Accept | Confirm assignment | `POST /rider/deliveries/{deliveryOrderPublicId}/accept` | No state change; just confirmation. |
| Reject | Unassign | `POST /rider/deliveries/{deliveryOrderPublicId}/reject` | Delivery back to PENDING; rider becomes available. |
| Start | Mark PICKED_UP or IN_TRANSIT | `POST /rider/deliveries/{deliveryOrderPublicId}/start?pickedUp=true/false` | |
| Update location (during delivery) | Tracking + update rider position | `POST /rider/deliveries/{deliveryOrderPublicId}/location` body: `latitude`, `longitude`, `note` | Also updates rider’s current_lat/lng for future dispatch. |
| Complete | Mark DELIVERED | `POST /rider/deliveries/{deliveryOrderPublicId}/complete` body: optional `podType`, `podPayload` | Rider becomes available again. |
| Upload POD | Photo/signature | `POST /rider/deliveries/{deliveryOrderPublicId}/pod?type=` + multipart file | |

**Flow:** Rider logs in → updates location (me/location) → sees assigned deliveries → start → (optional location updates) → complete (and optionally upload POD). Reject or complete frees the rider (available again).

---

### 4.10 Onboarding (merchant – business & store)

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| Create business | Step 1 after user signup | `POST /onboarding/businesses` body: name, tradeName, etc. | |
| Get business | | `GET /onboarding/businesses/{businessPublicId}` | |
| Create store | Under business | `POST /onboarding/businesses/{businessPublicId}/stores` body: name, domainSlug, etc. | |
| Upload compliance docs | CAC, TIN, ID | `POST /onboarding/businesses/{businessPublicId}/documents?documentType=` + file | |
| Submit for verification | Send to admin | `POST /onboarding/businesses/{businessPublicId}/submit` | |
| Add bank account | For payouts | `POST /onboarding/stores/{storePublicId}/bank-accounts` | |

**Connections:** Store belongs to business. Catalog, inventory, orders, delivery, POS are per **store**. Store has a **profile** (address, lat/lng, logo, etc.); admin assigns store to a **service zone** for delivery.

---

### 4.11 Catalog (merchant)

Products and variants are **store-scoped**. Collections and tags can group products.

| Feature | Description | API (base path `/stores/{storePublicId}/...`) | Usage |
|--------|-------------|-----------------------------------------------|--------|
| Products | CRUD products | Catalog controller: list, get, create, update, delete | Product has variants (e.g. size, color); variants have price, SKU. |
| Collections | Group products | Collection controller | For storefront grouping. |
| Uploads | Product images etc. | Upload controller | Store files; use returned URL in product/variant. |

**Connections:** Inventory is per **location** and **variant**. Discovery availability is derived from inventory at a **location**. Storefront lists products by store slug.

---

### 4.12 Inventory (merchant)

Inventory items are per **location** (branch) and **variant**. Locations belong to a store.

| Feature | Description | API | Usage |
|--------|-------------|-----|--------|
| Locations | CRUD locations | Inventory controller: locations | Each location can have address, city, state, lat/lng. |
| Inventory items | Stock per location × variant | Create, adjust quantity | Discovery availability is built from these. |
| Adjustments | Change quantity | Inventory adjustment API | |

**Connections:** Discovery `availability` = inventory at that **location** for requested **variants**. Cart uses **variantPublicId**; checkout creates order items from cart (variant + quantity).

---

### 4.13 POS (merchant – offline-first)

POS is for in-store sales: registers, sessions, offline transactions, sync, cash drawer.

| Feature | Description | API (base `/stores/{storePublicId}/pos`) | Usage |
|--------|-------------|------------------------------------------|--------|
| Registers | Create/list registers | POST/GET registers | One register per device/counter. |
| Sessions | Open/close session | Open session, close session | Tracks cash drawer. |
| Offline transactions | Record sales offline | Submit offline transaction(s) | Sync when back online. |
| Sync | Resolve conflicts, push synced data | POSSync request/response | Conflict resolution and state sync. |
| Cash movements | In/out from drawer | Cash movement APIs | |

**Connections:** POS is store-scoped; uses same store and permissions (e.g. orders:write). Independent from online cart/checkout; can share catalog/inventory conceptually but flows are separate.

---

### 4.14 Admin

**Base path:** `/admin/**`. **Auth:** Admin login; roles: PLATFORM_ADMIN, SUPERUSER, SUPPORT.

| Area | Feature | API / usage |
|------|--------|-------------|
| Auth | Login, complete setup | `POST /admin/auth/login`, `POST /admin/auth/complete-setup` |
| Users | List/update admins | Admin user management |
| Businesses | List, verify, update | Business verification (e.g. approve/reject after docs) |
| Roles & permissions | Platform roles, permissions | Role management, assign permissions to roles |
| Merchant permissions | Merchant-level roles/permissions | Admin merchant role/permission controllers |
| **Logistics** | **Service zones** | `GET/POST /admin/logistics/service-zones`, `GET/PATCH /admin/logistics/service-zones/{zonePublicId}` |
| | **Assign store to zone** | `PATCH /admin/logistics/stores/{storePublicId}/service-zone` body: `{"zonePublicId":"..."}` |
| | **Riders** | `GET/POST /admin/logistics/riders`, `GET/PATCH /admin/logistics/riders/{riderPublicId}` (filter by status or zonePublicId). Create/update use `zonePublicId`. |

**Service zone model (Chowdeck):** Zone has name, city, center_lat/lng, radius_km, base_fee, per_km_fee, max_distance_km, min_order_amount, surge_enabled, active. Stores are assigned to one zone; riders are assigned to one zone and report location; delivery creation checks customer in zone, computes fee, assigns nearest available rider in zone.

---

## 5. Key end-to-end flows (summary)

### 5.1 Shopper: discover → cart → pay → (optional) delivery

1. **Discover:** Search stores (discovery) or open store by slug (storefront). Pick location; get availability (variants + prices).
2. **Cart:** Add variants to cart (store publicId + cart token). Optionally apply discount code (pricing/apply).
3. **Checkout:** Create order and pay in one call; redirect to gateway; on return, verify payment. Order is PAID.
4. **Delivery (optional):** If merchant supports delivery, collect address and **customer lat/lng**, then merchant (or app) creates delivery order with orderPublicId + deliveryLat + deliveryLng. System assigns nearest rider; rider app shows the job.

### 5.2 Merchant: order received → fulfill → delivery (if needed)

1. **Orders:** List orders (filter by status); open order detail. Update status: CONFIRMED → PROCESSING.
2. **Delivery:** For a PAID order that needs delivery, create delivery order with customer address and lat/lng. System assigns rider; merchant sees delivery with rider and status.
3. **Rider:** Rider gets assignment, starts, completes (or rejects). Merchant can list deliveries and reschedule if FAILED/RETURNED.

### 5.3 Rider: go online → get assigned → deliver

1. **Login** (rider auth); **update location** (me/location) so dispatch can pick nearest.
2. **My deliveries:** See ASSIGNED/PICKED_UP/IN_TRANSIT. Accept or reject; start; update location; complete (and optionally upload POD). On complete/reject, rider becomes available again.

---

## 6. IDs and path conventions

- **Store:** Use `storePublicId` in API paths (e.g. `/stores/{storePublicId}/cart`). Storefront uses `storeSlug` (e.g. `/storefront/coffee-shop`). Resolve slug → store (get publicId from response).
- **Cart:** Cart has `publicId` (use as `cartId` in checkout). Use `X-Cart-Token` for anonymous persistence.
- **Order:** `orderPublicId` – used in delivery creation and order detail.
- **Delivery order:** `deliveryOrderPublicId` – used in delivery list, assign, reschedule, and rider flows.
- **Location:** `locationPublicId` – from discovery locations list; used in availability and conceptually in inventory.
- **Variant:** `variantPublicId` – from availability or product detail; used in cart items.
- **Rider:** `riderPublicId` – from admin riders or delivery response; used in manual assign.
- **Service zone:** `zonePublicId` – from admin service zones; used when assigning store to zone or creating/updating riders.

---

## 7. Errors and validation

- **401 Unauthorized:** Missing or invalid token; re-login or refresh.
- **403 Forbidden:** Valid user but no permission for this store or action (e.g. missing orders:write).
- **404:** Resource not found (wrong publicId or store).
- **400 / 422:** Validation (e.g. missing deliveryLat/deliveryLng, customer outside zone, no available riders, order not PAID). Show backend error message to user where appropriate.

---

## 8. What to build per surface

- **Web/Mobile (shopper):** Discovery (search, map/list, locations, availability), storefront (store by slug, products), cart (token-based), checkout (create-order-and-pay, redirect, verify), optional “Request delivery” with address + lat/lng.
- **Web (merchant):** Onboarding (business, store, docs), catalog (products, variants, collections), inventory (locations, stock), orders (list, detail, status), delivery (list, create with lat/lng, assign/reschedule), pricing (rules, codes), POS (registers, sessions, sync).
- **Mobile (rider):** Login, update my location, my deliveries list/detail, accept/reject, start, location updates, complete, POD upload.
- **Web (admin):** Login, businesses (list, verify), users, roles/permissions, logistics (service zones CRUD, assign store to zone, riders CRUD).

This document reflects the current backend behaviour and is the single reference for how features connect and how they are meant to work for frontend and mobile engineers building without Figma.
