# User Stories: Landing Page

Public-facing landing page (no auth required). For discovery and storefront browsing, see **Securemarts Users App** (`user-stories-customer.md`).

**Relevant APIs:** `GET /public/subscription-plans`, `GET /storefront/{storeSlug}` (optional), `/auth/*` (links).

---

## Stories

| As a... | I want to... | So that... | API(s) |
| ------- | ------------------------------------------------------------------------ | -------------------------------------------- | ---------------------------------- |
| Visitor | See subscription plans (Basic, Pro, Enterprise) with limits and features | I can compare plans before signing up | `GET /public/subscription-plans` |
| Visitor | Navigate to "Sign up" / "Login" | I can become a merchant or customer | Link to auth flows using `/auth/*` |
| Visitor | See a CTA to "View sample store" (optional) | I can preview a storefront before committing | `GET /storefront/{storeSlug}` |

---

## Out of scope for landing

Discovery and storefront browsing are better suited to the **Securemarts Users App**. The landing page can link to that app or to merchant signup.

---

## Dev handoff

- **Story format:** "As a [persona], I want to [action], so that [benefit]."
- **Acceptance criteria:** Use the listed API(s); include success/error handling.
- **API contract:** See OpenAPI/Swagger or backend DTOs for request/response shapes.

See also: [USER_STORIES.md](USER_STORIES.md) for the full set of stories across all apps.
