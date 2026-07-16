# Webstore Cart, Checkout, and Payment System Progression

> **Document Type:** System Progression  
> **Source of Truth For:** Audited implementation, integration, and verification status for carts, checkout sessions, payment providers, and webhooks  
> **Must Not Define:** Canonical pricing, fraud, refund, provider, credential, or settlement rules  
> **Current Status:** Implemented / Testing  
> **Last Audited Commit:** `afa6994109f2c1fb861fe891a338cd8b161aed28`

## 1. About

Tracks anonymous cart sessions, cart-item mutation, checkout-session creation and status, Stripe and PayPal integration surfaces, simulated sandbox success, payment webhooks, and administrator payment configuration.

## 2. Ownership Rules

### This Document Owns

- Cart and checkout implementation status.
- Provider configuration and webhook integration evidence.
- Payment-to-order transition and retry coverage.

### Connected Ownership

Catalog owns current product and offer truth. Orders own durable purchased results. Platform identity does not automatically own storefront customer identity. External providers own payment authorization and settlement truth delivered through verified APIs and webhooks.

### This Document Must Not Define

- New payment, refund, fraud, tax, or checkout behavior.
- Provider credentials or secret values.
- Successful payment claims without provider-backed evidence.

## 3. Progress Summary

| System Area | Status | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Cart sessions | `Implemented / Existing` | Cart-session and cart services, item mutation routes, counts, and storefront integration are present. | Verify expiry, concurrency, invalid products, quantity bounds, and price refresh. |
| Checkout sessions | `Implemented / Existing` | Checkout create and status routes, session models, and storefront views are present. | Verify idempotency, abandoned sessions, duplicate submits, and stale carts. |
| Stripe and PayPal surfaces | `Implemented / Testing` | Provider settings, webhook routes, provider fields, sandbox configuration, and payment tests are present. | Execute real sandbox authorization, cancellation, failure, and webhook-signature flows. |
| Payment-to-order transition | `Implemented / Testing` | Payment status and order creation/update behavior are represented in services and tests. | Verify exactly-once order mutation under duplicate and out-of-order webhooks. |

## 4. Implementation Checklist

- [x] Owning system boundary and representative production evidence were audited.
- [x] Exact audit boundary is recorded.
- [ ] Cart, checkout, and webhook tests were executed.
- [ ] Stripe sandbox flows were executed.
- [ ] PayPal sandbox flows were executed.
- [ ] Duplicate, delayed, invalid-signature, refund, and cancellation cases were verified.

## 5. Validation Evidence

| Validation Type | Command or Artifact | Result | Audited Commit |
| --- | --- | --- | --- |
| Repository audit | Cart, checkout, payments, webhook, settings, controller, template, and persistence packages | Evidence present | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Automated tests | `CartServiceTest`, `PaymentWebhookServiceTest`, storefront/controller test sources | Present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Provider operation | Stripe and PayPal sandbox checkout and signed webhook delivery | Not run | `afa6994109f2c1fb861fe891a338cd8b161aed28` |

## 6. Blockers and Risks

- Provider secrets must never be rendered, logged, committed, or returned through administrator views.
- Duplicate or reordered webhooks can duplicate orders or settlement changes without idempotency constraints.
- Client-visible success pages must not be treated as authoritative payment confirmation.
- Refund, chargeback, tax, and subscription-renewal behavior remain unaudited.

## 7. Next Implementation Slice

1. Run cart, checkout, and webhook tests.
2. Exercise one Stripe and one PayPal sandbox purchase through signed webhook settlement.
3. Add duplicate, delayed, failed, cancelled, and invalid-signature cases.
4. Draft the canonical checkout and payment contract after provider-backed verification.

## 8. Update Rules

Follow [the progression template](../progression/SYSTEM_PROGRESSION_TEMPLATE.md). A simulated success endpoint is test support, not evidence that money moved anywhere except inside a developer's imagination.
