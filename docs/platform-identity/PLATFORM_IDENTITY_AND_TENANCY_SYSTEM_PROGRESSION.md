# Webstore Platform Identity and Tenancy System Progression

> **Document Type:** System Progression  
> **Source of Truth For:** Audited implementation, integration, and verification status for platform identity and tenant administration  
> **Must Not Define:** Canonical authentication policy, role contracts, schemas, or tenant product rules  
> **Current Status:** Implemented / Existing  
> **Last Audited Commit:** `afa6994109f2c1fb861fe891a338cd8b161aed28`

## 1. About

Tracks provider-based platform authentication, tenant accounts, user roles, onboarding, and master-administration surfaces used to operate Webstore tenants.

This tracker reports repository evidence. Authentication and authorization were not exercised against real identity providers during this documentation pass.

## 2. Ownership Rules

### This Document Owns

- Provider-login and platform-session implementation status.
- Tenant-account and platform-role persistence evidence.
- Onboarding and administrative tenant-management coverage.

### Connected Ownership

Tenant sites and publications own storefront deployment state. The tenant runtime system owns cluster resources. Storefront customer identity is a separate commerce concern and must not silently inherit platform-administrator authority.

### This Document Must Not Define

- New account, role, onboarding, or provider behavior.
- Canonical security configuration or credential-storage rules.
- A verified authentication result without provider-backed execution evidence.

## 3. Progress Summary

| System Area | Status | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Provider authentication | `Implemented / Existing` | `platform-spring-webview` includes Spring Security and OAuth2 client support with provider entry and callback routes. | Execute login, callback, logout, failed-provider, and disabled-provider flows. |
| Tenant accounts | `Implemented / Existing` | Platform persistence includes tenant-account entities, repositories, and Flyway-managed platform schemas. | Audit uniqueness, suspension, deletion, and recovery behavior. |
| Platform roles | `Implemented / Existing` | Platform sessions and master-admin email configuration gate administrative surfaces. | Promote exact role and authority rules into an owning final draft. |
| Onboarding and administration | `Implemented / Existing` | `/onboarding`, `/app`, `/admin/tenants`, and related server-rendered routes are documented and implemented. | Run browser-level authorization and tenant-isolation checks. |

## 4. Implementation Checklist

- [x] Owning system boundary and representative production evidence were audited.
- [x] Exact audit boundary is recorded.
- [ ] Automated verification was executed at the audited commit.
- [ ] Provider-backed browser verification was executed.
- [ ] Tenant isolation and role escalation were operationally tested.

## 5. Validation Evidence

| Validation Type | Command or Artifact | Result | Audited Commit |
| --- | --- | --- | --- |
| Repository audit | `platform-internal-api` identity, persistence, and tenant packages; `platform-spring-webview` security and onboarding routes | Evidence present | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Automated tests | Relevant platform security and controller test sources | Present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Browser and OAuth flow | Real provider login and authorization matrix | Not run | `afa6994109f2c1fb861fe891a338cd8b161aed28` |

## 6. Blockers and Risks

- Provider configuration and callback behavior remain unverified against real external identity systems.
- Platform administration and storefront customer access must remain separate authority domains.
- Tenant suspension and deletion need explicit data-retention and runtime-shutdown rules.

## 7. Next Implementation Slice

1. Run the platform identity and controller test suites.
2. Exercise provider login, onboarding, role denial, logout, and tenant isolation in a browser.
3. Draft the canonical identity, role, and tenant-lifecycle contract from verified behavior.

## 8. Update Rules

Follow [the progression template](../progression/SYSTEM_PROGRESSION_TEMPLATE.md). Record exact commands and results before promoting any area to `Verified`.
