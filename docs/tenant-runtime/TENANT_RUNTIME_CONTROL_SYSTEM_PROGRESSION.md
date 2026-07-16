# Webstore Tenant Runtime Control System Progression

> **Document Type:** System Progression  
> **Source of Truth For:** Audited implementation, integration, and verification status for isolated tenant runtime orchestration  
> **Must Not Define:** Canonical infrastructure topology, runtime SLA, native-build contract, or tenant-site product behavior  
> **Current Status:** Implemented / Testing  
> **Last Audited Commit:** `afa6994109f2c1fb861fe891a338cd8b161aed28`

## 1. About

Tracks the Java control plane that provisions and operates isolated tenant storefront runtimes through Kubernetes and KubeVirt, including compatibility evaluation, lifecycle mutation, routing resources, status synchronization, and host-profile operations.

The repository contains substantial orchestration implementation and tests. No real cluster was operated during this documentation pass.

## 2. Ownership Rules

### This Document Owns

- Kubernetes and KubeVirt control-plane implementation status.
- Runtime launch, stop, restart, destroy, resource mutation, and status evidence.
- Cluster compatibility, host-profile, and reconciliation coverage.
- Recovery status for the historical GraalVM/native-image expectation.

### Connected Ownership

Tenant sites own desired publication and domain state. Storefront code owns the guest workload. Platform identity owns authorization. Kubernetes and KubeVirt own actual scheduling and virtualization behavior after resources are submitted.

### This Document Must Not Define

- New infrastructure, scaling, isolation, or native-image behavior.
- Production capacity, reliability, or security claims without cluster evidence.
- GraalVM implementation status inferred from memory or intended architecture.

## 3. Progress Summary

| System Area | Status | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Internal control API | `Implemented / Existing` | Launch, stop, restart, destroy, resource mutation, infrastructure-profile mutation, domain, publication, sync, status, and compatibility endpoints are present. | Execute authorization and lifecycle flows against a database and cluster. |
| Kubernetes and KubeVirt provisioning | `Implemented / Existing` | Fabric8-based control code creates namespaces, Secrets, KubeVirt virtual machines, Services, and Ingress resources. | Verify apply, partial failure, idempotency, cleanup, and restart recovery. |
| Compatibility and placement | `Implemented / Testing` | Tests cover dedicated and nested KubeVirt, `/dev/kvm`, x86 emulation, arm64 rejection, and node selectors. | Run tests and compare decisions with real cluster nodes. |
| Runtime reconciliation | `Implemented / Existing` | A scheduled synchronization job reconciles provisioning, running, updating, and stopped sites. | Validate missed events, stale status, concurrent mutation, and controller restart. |
| Host operations | `Implemented / Existing` | `ops/kubevirt` detects and configures dedicated or nested host profiles, labels nodes, and manages `useEmulation`. | Exercise scripts on supported K3s or Kubernetes hosts. |
| GraalVM native build | `Not Audited / Unresolved` | Historical expectation says the tenant platform targeted GraalVM, but current and preserved visible Maven, operations, Docker-related, and commit evidence does not expose a Native Image or Spring AOT configuration. | Locate deleted, local, or unmerged monorepo evidence, or reintroduce the build deliberately before claiming it. |

## 4. Implementation Checklist

- [x] Owning system boundary and representative production evidence were audited.
- [x] Exact audit boundary and preserved subsystem commits are recorded.
- [ ] KubeVirt and control-plane automated tests were executed at the audited commit.
- [ ] A real Kubernetes or KubeVirt lifecycle was executed.
- [ ] Failure, restart, reconciliation, and cleanup behavior were operationally verified.
- [ ] GraalVM Native Image configuration was located or rebuilt and measured.

## 5. Validation Evidence

| Validation Type | Command or Artifact | Result | Audited Commit |
| --- | --- | --- | --- |
| Repository audit | `platform-internal-api` control, runtime, persistence, and Fabric8 integration packages | Evidence present | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Automated tests | `PlatformInternalApiDelegationIntegrationTest`, `KubeVirtClusterCompatibilityEvaluatorTest`, `TenantSiteRuntimeSpecFactoryTest`, lifecycle tests | Present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Host operations | `ops/kubevirt/README.md`, detection script, configuration script, dedicated and nested profiles | Evidence present; not executed | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| Real cluster | Launch, route, stop, restart, reconcile, and destroy a tenant runtime | Not run | `afa6994109f2c1fb861fe891a338cd8b161aed28` |
| GraalVM search | Current and preserved visible POMs, operations snapshots, build terminology, branches, and commit messages | No recoverable configuration found | `afa6994109f2c1fb861fe891a338cd8b161aed28` |

## 6. Blockers and Risks

- A control-plane implementation is not proof that real KubeVirt operation succeeded.
- Partial resource creation can leave namespace, Secret, VM, Service, Ingress, database, and publication state inconsistent.
- Shared-secret internal API security, kubeconfig access, and tenant isolation require operational review.
- GraalVM remains a historical expectation, not a verified current capability.

## 7. Next Implementation Slice

1. Run the platform control and compatibility test suites.
2. Execute dry-run, then real-cluster launch, readiness, routing, restart, reconciliation, and destruction.
3. Recover or deliberately implement the intended GraalVM Native Image build and record startup time, memory, image size, and compatibility limits.
4. Separate reusable tenant runtime control from Webstore only through a later focused architecture pass, not this documentation-only change.

## 8. Update Rules

Follow [the progression template](../progression/SYSTEM_PROGRESSION_TEMPLATE.md). Never turn intended infrastructure, test presence, or dry-run behavior into a production-operation claim.
