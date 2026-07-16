# Webstore

<!-- tavall:badges:start -->
[![Org](https://img.shields.io/badge/org-TavallStudios-181717?logo=github)](https://github.com/TavallStudios) [![Stack](https://img.shields.io/badge/stack-Java%2021%20%7C%20Spring%20%7C%20PostgreSQL%20%7C%20Kubernetes-0A7BBB)](https://api.github.com/repos/TavallStudios/Webstore) ![History](https://img.shields.io/badge/history-preserved-6f42c1)
<!-- tavall:badges:end -->

Webstore is a rules-driven commerce platform for operating configurable tenant storefronts. It combines a Spring-based seller and storefront application with a control plane capable of provisioning isolated tenant runtimes through Kubernetes and KubeVirt.

The product direction is intentionally stricter than a completely open site builder: sellers receive substantial control over catalog, presentation, offers, payments, and operations while the platform retains enforceable security, publication, infrastructure, and quality boundaries.

The Minecraft-specific Project Novus web store is owned by Project Novus and is not part of this repository.

## Repository Structure

| Module | Responsibility |
| --- | --- |
| `platform-internal-api` | Platform authentication, tenant and site persistence, publications, audit state, runtime control, and internal control APIs. |
| `platform-spring-webview` | Spring Boot entrypoint for the platform website, provider login, onboarding, master administration, and hosted internal control API. |
| `webstore-view` | Standalone tenant storefront and storefront-administration application. |

Supporting infrastructure includes PostgreSQL migrations, KubeVirt host-profile operations, frontend assets, and local test guidance.

## Implemented Surfaces

### Commerce

- Products and product detail
- Cart and checkout sessions
- Bundle offers and subscription plans
- Stripe and PayPal integration surfaces
- Signed webhook handling paths
- Orders, fulfillment, shipments, confirmation, and tracking
- Store settings, configurable pages, sections, feature flags, analytics, and engagement controls
- Media and theme administration

### Platform

- Provider-based platform authentication
- Tenant onboarding and administration
- Tenant sites, domains, publications, and runtime definitions
- Internal launch, stop, restart, destroy, mutate, synchronize, status, and compatibility APIs
- Kubernetes namespace, Secret, Service, and Ingress orchestration
- KubeVirt virtual-machine lifecycle and dedicated or nested host profiles
- Scheduled runtime-status reconciliation

## Documentation

Start with the [documentation index](docs/README.md).

- [Code architecture policy](docs/quality/CODE_ARCHITECTURE.md)
- [Git and review workflow](docs/quality/GIT_WORKFLOW.md)
- [Documentation standards](docs/quality/DOCUMENTATION_STANDARDS.md)
- [System progression index](docs/progression/README.md)
- [Tenant runtime control progression](docs/tenant-runtime/TENANT_RUNTIME_CONTROL_SYSTEM_PROGRESSION.md)

Progression documents distinguish implementation evidence from tests or operations that were actually executed. No current system is marked `Verified` merely because source and test files exist. Civilization has endured enough dashboards declaring victory before anyone pressed the button.

## Local Development

Build all modules:

```bash
./mvnw -q test
./mvnw -q -DskipTests package
```

Run the platform application:

```bash
./mvnw -pl platform-spring-webview -am spring-boot:run
```

Run a tenant storefront:

```bash
./mvnw -pl webstore-view -am spring-boot:run
```

PostgreSQL and provider configuration are required for complete platform, payment, and persistence flows. Use the repository's local test guide for current ports, environment variables, dry-run control behavior, and KubeVirt test requirements.

## Verification Boundary

The repository contains production source, migrations, integration boundaries, tests, and KubeVirt operations tooling. The current documentation audit did not execute:

- the full Maven test suite;
- real OAuth provider flows;
- Stripe or PayPal sandbox settlement;
- browser accessibility and responsive checks;
- PostgreSQL migration recovery;
- a real Kubernetes or KubeVirt lifecycle;
- a GraalVM Native Image build.

GraalVM remains an unresolved historical expectation for the tenant infrastructure platform. The currently recoverable build files do not establish an active Native Image or Spring AOT configuration.

## History

This repository was extracted from `TavallMonoRepo` on April 24, 2026 with its project history preserved.
