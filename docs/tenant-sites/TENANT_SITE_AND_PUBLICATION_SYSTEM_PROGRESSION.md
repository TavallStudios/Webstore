# Webstore Tenant Site and Publication System Progression

> **Document Type:** System Progression  
> **Source of Truth For:** Audited implementation, integration, and verification status for tenant sites, domains, and publications  
> **Must Not Define:** Canonical site lifecycle, domain policy, deployment behavior, or persistence contracts  
> **Current Status:** Implemented / Existing  
> **Last Audited Commit:** `afa6994109f2c1fb861fe891a338cd8b161aed28`

## 1. About

Tracks tenant-owned storefront sites, runtime definitions, domain assignment, publication versions, readiness, and administrator visibility.

## 2. Ownership Rules

### This Document Owns

- Tenant-site domain and persistence implementation status.
- Domain-assignment and publication-operation evidence.
- Site lifecycle and administrative visibility coverage.

### Connected Ownership

Platform identity owns tenant authority. Tenant runtime control owns Kubernetes and KubeVirt resources. The storefront owns rendering of a published version.

### This Document Must Not Define

- New site, domain, publication, or readiness behavior.
- Canonical lifecycle transitions or deletion rules.
- Verified publication without database, runtime, and browser evidence.

## 3. Progress Summary

| System Area | Status | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Tenant site records | `Implemented / Existing` | `TenantSite`, runtime-definition, publication, and domain persistence models are present in `platform-internal-api`. | Audit constraints, tenancy isolation, and deletion behavior. |
| Domain assignment | `Implemented / Existing` | Internal and administrative mutation routes assign domains through the control service. | Verify collision, invalid-host, reassignment, and rollback paths. |
| Version publication | `Implemented / Existing` | Publication commands, entities, routes, and administration views are present. | Verify immutable versioning, artifact integrity, and failed rollout recovery. |
| Readiness and status | `Implemented / Existing` | Mark-ready, status, sync, deployment history, and operations surfaces exist. | Define the authoritative readiness gate and exercise it end to end. |

## 4. Implementation Checklist

- [x] Owning system boundary and representative production evidence were audited.
- [x] Exact audit boundary is recorded.
- [ ] Automated verification was executed at the audited commit.
- [ ] Database-backed publication and rollback were executed.
- [ ] A published tenant site was verified through its assigned domain.

## 5. Validation Evidence

| Validation Type | Command or Artifact | Result | Audited Commit |
| --- | --- | --- | --- |
| Repository audit | Platform site, domain, publication, lifecycle, persistence, controller, and admin packages | Evidence present | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Automated tests | Runtime-spec, lifecycle-transition, controller, and persistence test sources | Present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Database and browser flow | Create site, assign domain, publish version, mark ready, render storefront | Not run | `afa6994109f2c1fb861fe891a338cd8b161aed28` |

## 6. Blockers and Risks

- Publication and runtime state may diverge without explicit reconciliation ownership.
- Domain collision, certificate, DNS, and ingress failure behavior need one final contract.
- Site deletion must define what happens to publications, domains, audit records, and runtime resources.

## 7. Next Implementation Slice

1. Run lifecycle and publication tests.
2. Exercise one complete site creation, domain assignment, publication, readiness, and rollback flow.
3. Promote lifecycle invariants and recovery rules into an owning final draft.

## 8. Update Rules

Follow [the progression template](../progression/SYSTEM_PROGRESSION_TEMPLATE.md). Keep publication intent, deployment state, and browser verification separate in all status claims.
