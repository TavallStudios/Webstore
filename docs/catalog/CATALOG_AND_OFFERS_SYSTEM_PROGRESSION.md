# Webstore Catalog and Offers System Progression

> **Document Type:** System Progression  
> **Source of Truth For:** Audited implementation, integration, and verification status for products, bundles, subscriptions, and catalog administration  
> **Must Not Define:** Canonical pricing, inventory, tax, merchandising, or offer-eligibility rules  
> **Current Status:** Implemented / Existing  
> **Last Audited Commit:** `afa6994109f2c1fb861fe891a338cd8b161aed28`

## 1. About

Tracks products, product detail, bundle offers, subscription plans, pricing fields, inventory-facing data, catalog administration, and storefront catalog lookup.

## 2. Ownership Rules

### This Document Owns

- Product and offer model implementation status.
- Catalog persistence, service, administration, and storefront lookup evidence.
- Bundle and subscription configuration coverage.

### Connected Ownership

Checkout owns cart pricing snapshots and purchase mutation. Payments own provider settlement. Orders own purchased line items. Content and themes own how catalog data is presented.

### This Document Must Not Define

- New pricing, inventory, subscription, tax, or promotion rules.
- Canonical product publication or deletion behavior.
- Verified pricing integrity without database and checkout evidence.

## 3. Progress Summary

| System Area | Status | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Products | `Implemented / Existing` | Product models, repositories, catalog services, public product lookup, and administrator forms are present. | Verify validation, uniqueness, inactive products, inventory boundaries, and concurrent edits. |
| Bundle offers | `Implemented / Existing` | Bundle-offer models, configuration, placement, discount fields, and administrator mutation routes are present. | Verify eligibility, pricing, stacking, expiry, and cart behavior. |
| Subscription plans | `Implemented / Existing` | Subscription-plan models and administrator configuration are present. | Verify billing-provider integration, cancellation, renewal, and failed-payment behavior. |
| Catalog persistence | `Implemented / Existing` | JPA, PostgreSQL, and Flyway-backed catalog structures are present. | Run repository tests and migration checks against PostgreSQL. |

## 4. Implementation Checklist

- [x] Owning system boundary and representative production evidence were audited.
- [x] Exact audit boundary is recorded.
- [ ] Catalog and repository tests were executed.
- [ ] PostgreSQL migrations and constraints were verified.
- [ ] Product, bundle, and subscription flows were exercised through storefront and checkout.

## 5. Validation Evidence

| Validation Type | Command or Artifact | Result | Audited Commit |
| --- | --- | --- | --- |
| Repository audit | `org.tavall.webstore.catalog` models, repositories, services, controllers, templates, and migrations | Evidence present | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Automated tests | Product repository and administrator rendering test sources | Present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Database and purchase flow | Create product, configure offer, render product, add to cart, purchase | Not run | `afa6994109f2c1fb861fe891a338cd8b161aed28` |

## 6. Blockers and Risks

- Pricing must not be trusted from customer-submitted cart or checkout payloads.
- Offer stacking, subscription renewal, and inventory reservation need explicit transactional ownership.
- Product deletion or deactivation must preserve historical order line items.

## 7. Next Implementation Slice

1. Run catalog repository and service tests against PostgreSQL.
2. Exercise active, inactive, invalid, bundle, and subscription catalog cases through checkout.
3. Draft canonical product, pricing, inventory, and offer rules from verified behavior.

## 8. Update Rules

Follow [the progression template](../progression/SYSTEM_PROGRESSION_TEMPLATE.md). Keep catalog configuration, checkout calculation, provider billing, and order history as separate ownership boundaries.
