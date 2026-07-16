# Webstore Documentation

Webstore documentation separates shared engineering policy, accepted system behavior, implementation evidence, and historical design material so each question has one clear owner.

## Start Here

| Need | Document |
| --- | --- |
| Shared code and module policy | [Code Architecture](quality/CODE_ARCHITECTURE.md) |
| Branches, commits, review, and promotion | [Git Workflow](quality/GIT_WORKFLOW.md) |
| Document types, ownership, lifecycle, and archives | [Documentation Standards](quality/DOCUMENTATION_STANDARDS.md) |
| Current implementation status | [System Progression Index](progression/README.md) |
| Progression document requirements | [System Progression Template](progression/SYSTEM_PROGRESSION_TEMPLATE.md) |

## Current System Boundaries

- Platform identity and tenant administration
- Tenant sites, domains, and publication
- Tenant runtime control through Kubernetes and KubeVirt
- Storefront rendering and customer-facing navigation
- Catalog, products, bundles, and subscriptions
- Cart, checkout, and payment-provider integration
- Orders, fulfillment, shipment tracking, and customer confirmation
- Site content, configuration, media, themes, analytics, and engagement controls

These boundaries describe ownership for documentation. They do not imply that every system is complete or operationally verified.

## Authority Order

1. An accepted `*_FINAL.md` document owns canonical product and technical behavior for its system.
2. A `*_PROGRESSION.md` document owns audited implementation and verification status at its recorded commit.
3. Active implementation references describe a narrow observed surface and must defer to owning final and progression documents.
4. Drafts propose behavior according to their lifecycle state.
5. Archived documents have no current authority.

Existing historical design prompts remain implementation context until they are deliberately classified, replaced, or archived. A large prompt is not automatically a final contract merely because it possesses the confidence and page count of a small constitution.
