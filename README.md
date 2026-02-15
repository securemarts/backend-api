# Shopper Platform

Multi-tenant Shopify-like commerce platform for the Nigerian market. Built with Java 17, Spring Boot, Spring Data JPA, Spring Security (JWT), PostgreSQL, and Flyway.

## Tech Stack

- **Java 17+**
- **Spring Boot 3.2**
- **Spring Data JPA** – persistence
- **Spring Security** – JWT access + refresh tokens, BCrypt, rate limiting, account lock
- **PostgreSQL** – database
- **Flyway** – migrations
- **Swagger / OpenAPI 3** – API docs
- **Bean Validation (Jakarta)** – request validation
- **Caffeine** – caching

## Architecture

- **Modular monolith** – one app, packages per domain (`auth`, `onboarding`, `catalog`, etc.)
- **DTO layer** – no entity exposure on API
- **Domain services** – business logic in services
- **Multi-tenant** – store/business isolation; UUID public IDs. **Catalog is business-scoped** (one product catalog per business; stores share it and keep their own inventory).
- **Auditing** – `createdAt`, `updatedAt` on entities
- **Soft delete** – where applicable (e.g. products)
- **Base path** – `/api/v1` (context path)

## Project Structure

```
src/main/java/com/shopper/
├── common/           # BaseEntity, ApiError, PageResponse, exceptions
├── config/           # JPA, OpenAPI, Cache
├── security/         # JWT, filters, rate limiting, tenant context
├── domain/
│   ├── auth/         # User, Role, Permission, RefreshToken, LoginSession
│   ├── onboarding/   # Business, Store, ComplianceDocument, BankAccount
│   └── catalog/      # Product, ProductVariant, ProductMedia, Collection, Tag
```

## Running the Application

### Quick start with Make

If you have `make` installed:

```bash
make          # show all commands
make start    # start app + Postgres in Docker (recommended)
make logs     # follow logs
make stop     # stop containers
```

See the [Makefile](Makefile) for all targets (`make run`, `make run-local`, `make test`, etc.).

### 1. Prerequisites

- **Java 17** or higher (`java -version`)
- **Maven 3.8+** (`mvn -v`) or use the included Maven wrapper (`./mvnw`)
- **PostgreSQL 14+** running locally (or Docker)

### 2. Database setup

Create the database and user (PostgreSQL):

```bash
# Using psql or your DB tool:
createdb shopper
createuser -P shopper   # enter password when prompted (e.g. shopper)
# Or with default trust for local dev:
createuser shopper
```

Default connection: `jdbc:postgresql://localhost:5432/shopper` with user `shopper` and password `shopper`. Override with environment variables:

- `DB_PASSWORD` – database password
- `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET` – min 32 characters each for JWT signing

### 3. Run the app

From the project root:

```bash
# With Maven installed:
mvn spring-boot:run

# Or with Maven wrapper (if present):
./mvnw spring-boot:run
```

To generate the Maven wrapper (so others can run without installing Maven), run once: `mvn -N wrapper:wrapper`.

On startup, Flyway will run migrations and the app will listen on **http://localhost:8080**.

- **API base URL:** `http://localhost:8080/api/v1`
- **Health:** `http://localhost:8080/api/v1/actuator/health` (if actuator is added later)

### 4. Run with Docker Compose

From the project root:

```bash
docker compose up -d
```

This starts PostgreSQL and the app. The app waits for the database to be healthy before starting. Flyway runs on first boot.

- **API base URL:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/api/v1/swagger-ui.html

To pass JWT or Paystack keys from your `.env`:

```bash
# Optional: create .env with JWT_ACCESS_SECRET, JWT_REFRESH_SECRET, APP_PAYMENT_PAYSTACK_SECRET_KEY, etc.
docker compose up -d
```

Stop: `docker compose down`. Data is kept in Docker volumes (`postgres_data`, `uploads_data`).

### 5. Swagger / OpenAPI (API docs)

Swagger is implemented with **springdoc-openapi**. It’s enabled by default and **no login is required** to open the docs.

| What        | URL |
|------------|-----|
| **Swagger UI** (interactive) | **http://localhost:8080/api/v1/swagger-ui.html** |
| **OpenAPI JSON** (raw spec)  | http://localhost:8080/api/v1/api-docs |

**Using Swagger UI:**

1. Open **http://localhost:8080/api/v1/swagger-ui.html** in your browser.
2. You’ll see all tags (Auth, Business & Store Onboarding, Product Catalog, Cart, Inventory, Orders, Payments, Pricing, etc.) and endpoints.
3. **Public endpoints** (e.g. `POST /auth/register`, `POST /auth/login`, `GET/POST .../cart/...`) can be called directly from “Try it out”.
4. **Protected endpoints** require a JWT:
   - Call `POST /auth/login` (or register), copy the `accessToken` from the response.
   - Click **“Authorize”** (top right), enter `Bearer <your-access-token>` (with the word “Bearer” and a space), then **Authorize**.
   - After that, “Try it out” on protected endpoints will send the token automatically.

The OpenAPI config (`OpenApiConfig`) registers a **Bearer JWT** security scheme so Swagger UI can send the token in the `Authorization` header.

## Example API Usage

### Register

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "merchant@example.com",
    "password": "SecurePass123!",
    "firstName": "Chidi",
    "lastName": "Okafor",
    "userType": "MERCHANT_OWNER"
  }'
```

**Example response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "merchant@example.com",
  "roles": ["MERCHANT_OWNER"],
  "storeId": null
}
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "merchant@example.com", "password": "SecurePass123!"}'
```

### Admin login (platform admin)

Admins use a separate login; the response includes `roles` and `scopes` (permission codes) for RBAC-aware UIs.

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@shopper.local", "password": "password"}'
```

Use the returned `accessToken` with **Authorize** in Swagger, then call **GET /admin/auth/me** to get current admin identity, roles, and scopes.

### Create Business

```bash
curl -s -X POST http://localhost:8080/api/v1/onboarding/businesses \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "legalName": "Acme Ventures Ltd",
    "tradeName": "Acme Store",
    "cacNumber": "RC123456",
    "taxId": "12345678-0001"
  }'
```

### Create Store

```bash
curl -s -X POST "http://localhost:8080/api/v1/onboarding/businesses/{businessPublicId}/stores" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Main Store",
    "domainSlug": "acme-main",
    "defaultCurrency": "NGN"
  }'
```

### Create Product

Products are **business-scoped**: the catalog is shared by all stores under the same business. The `storePublicId` in the path is the store context (for auth and access); the product is created in that store’s business catalog.

```bash
curl -s -X POST "http://localhost:8080/api/v1/stores/{storePublicId}/products" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Organic Cotton T-Shirt",
    "bodyHtml": "<p>Comfortable unisex tee.</p>",
    "status": "ACTIVE",
    "variants": [
      {
        "sku": "TSHIRT-S-BLK",
        "title": "Small / Black",
        "priceAmount": 4500.00,
        "currency": "NGN",
        "position": 0
      }
    ],
    "media": [
      { "url": "https://example.com/tshirt.jpg", "alt": "T-Shirt", "position": 0 }
    ]
  }'
```

### List Products (paginated)

Lists the **business catalog** for the store (all stores under the same business see the same products). Response includes `businessId`; inventory is per store.

```bash
curl -s "http://localhost:8080/api/v1/stores/{storePublicId}/products?status=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Example response:**

```json
{
  "content": [
    {
      "publicId": "...",
      "title": "Organic Cotton T-Shirt",
      "handle": "organic-cotton-t-shirt",
      "status": "ACTIVE",
      "businessId": 1,
      "variants": [...],
      "media": [...]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Standard Error Response

All errors use a consistent shape:

```json
{
  "timestamp": "2025-02-13T10:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "fieldErrors": [
    { "field": "email", "message": "must be a valid email", "rejectedValue": "bad" }
  ]
}
```

## Security

- **BCrypt** (strength 12) for passwords
- **JWT** access (short TTL) + refresh tokens (long TTL); refresh tokens stored hashed
- **Rate limiting** on login (configurable per minute)
- **Account lock** after N failed attempts (configurable)
- **RBAC** – role- and permission-based access for both platform admins and merchant staff (see below)
- **Tenant context** – `CurrentTenant` holds store ID from JWT for multi-tenant checks

## RBAC (Roles & Permissions)

### Platform admin

- **Separate admin identity** – admins use `POST /admin/auth/login` (not user login). Tables: `admins`, `admin_roles`, `admin_invites`.
- **Roles:** `SUPERUSER`, `PLATFORM_ADMIN`, `SUPPORT` (an admin can have multiple roles).
- **Permissions (scopes):** Stored in `admin_permissions` and mapped per role in `admin_role_permissions`. Examples: `business:list`, `business:approve`, `admin:list`, `admin:read`, `admin:create`, `admin:invite`, `admin:update`, `admin:delete`. SUPERUSER bypasses all; others are restricted by scope.
- **Visibility:** Login response includes `roles` and `scopes`. Use **GET /admin/auth/me** (with admin JWT) to get current admin’s `publicId`, `email`, `fullName`, `roles`, and `scopes` for UI (e.g. show/hide Business Management, User Management, invite).
- **Controllers:** Admin Auth, Business Management (list/approve businesses), User Management (list/get/update/delete admins, invite, complete-setup). All protected by role + `@PreAuthorize` with `SCOPE_<code>` where applicable.

### Merchant (store) staff

- **Business members** – table `business_members`: role `MANAGER`, `CASHIER`, or `STAFF`; status `INVITED` or `ACTIVE`. Owners have full access to their businesses/stores.
- **Permissions:** Stored in `merchant_permissions` and mapped per role in `merchant_role_permissions`. Examples: `products:read`, `products:write`, `orders:read`, `orders:write`, `inventory:read`, `inventory:write`, `pricing:read`, `pricing:write`, `customers:read`, `customers:write`, `store:settings`. MANAGER gets all; CASHIER/STAFF get subsets.
- **Enforcement:** After **store access** (`StoreAccessService.ensureUserCanAccessStore`), **merchant permission** is checked with `MerchantPermissionService.ensureStorePermissionByPublicId` in:
  - **Catalog** – list/get → `products:read`; create/update/delete → `products:write`
  - **Inventory** – list locations/items/low-stock → `inventory:read`; create location/item, adjust, reserve, release → `inventory:write`
  - **Orders** – list/get → `orders:read`; update status → `orders:write`
  - **Pricing** – list/get rules → `pricing:read`; create/update rules, add discount code → `pricing:write`
- Cart and Payment endpoints are storefront/customer-facing and do not use merchant RBAC.

### Seeded admin (dev)

- Migration **V12** seeds a default admin: `admin@shopper.local` / `password`. Change in production.

## Database Migrations

- `V1__auth_schema.sql` – users, roles, permissions, refresh_tokens, login_sessions, password_reset_tokens
- `V2__onboarding_schema.sql` – businesses, stores, business_owners, compliance_documents, bank_accounts, store_profiles
- `V3__seed_roles_permissions.sql` – default roles and permissions
- `V4__product_catalog_schema.sql` – products, variants, media, collections, tags, metafields
- `V5__inventory_orders_cart_schema.sql` – locations, inventory_items, inventory_movements, carts, cart_items, orders, order_items
- `V8__catalog_business_scope.sql` – products, collections, tags scoped to business (not store); stores keep per-store inventory
- `V10` – admins table (platform admin identity)
- `V11` – business_members (staff roles: MANAGER, CASHIER, STAFF)
- `V12` – seed first admin (admin@shopper.local)
- `V13` – admin_invites (invite-by-email flow)
- `V14` – admin_roles (multi-role per admin)
- `V15` – admin RBAC (admin_permissions, admin_role_permissions)
- `V16` – merchant RBAC (merchant_permissions, merchant_role_permissions)

## Optional Follow-ups

- Nigerian payment gateways (Paystack, Flutterwave)
- Kafka for domain events
- Multi-currency
- RBAC dashboard
- Audit logging

## License

Proprietary.
