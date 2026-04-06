# Local Test Guide

This file is the current local testing reference for the active three-module repo:

- `platform-internal-api`: shared Spring module for platform auth, persistence, control logic, and internal control endpoints.
- `platform-spring-webview`: Spring Boot app for the platform site, provider login flow, onboarding UI, admin UI, and hosted internal control API.
- `webstore-view`: Spring Boot app for the tenant storefront and storefront admin UI.

## Current Local Ports

- Platform web and internal control API: `http://localhost:8081`
- Webstore view: `http://localhost:8080`

Notes:

- `platform-spring-webview` defaults to port `8081` via `PLATFORM_WEBVIEW_PORT` or `PLATFORM_WEB_PORT`.
- `webstore-view` uses the Spring default port `8080` unless `SERVER_PORT` is set.

## Quick Start

Run the platform site:

```powershell
.\mvnw -pl platform-spring-webview -am spring-boot:run
```

Run the storefront:

```powershell
.\mvnw -pl webstore-view -am spring-boot:run
```

Run the full test suite:

```powershell
.\mvnw -q test
```

Build all three modules:

```powershell
.\mvnw -q -DskipTests package
```

Run only the platform internal API tests:

```powershell
.\mvnw -q -pl platform-internal-api -am test
```

Run only the storefront tests:

```powershell
.\mvnw -q -pl webstore-view -am test
```

## Local Dependencies

- PostgreSQL is expected at `jdbc:postgresql://localhost:15432/tavall` by default.
- Platform schemas are managed by Flyway in the platform app: `platform_auth`, `platform_core`, `platform_sites`, and `platform_audit`.
- Storefront data defaults to the `public` schema unless `STOREFRONT_DATABASE_SCHEMA` is set.
- OAuth providers are optional for page rendering, but they are required for real login and onboarding testing.
- There is no local password flow. Platform login is provider-only by design.

## Platform Environment Variables

Useful platform variables for local manual testing:

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`: platform database connection.
- `PLATFORM_MASTER_ADMIN_EMAILS`: comma-separated emails that should receive `MASTER_ADMIN`.
- `PLATFORM_INTERNAL_API_SHARED_SECRET`: shared secret for `/internal/control/**`; default is `change-me`.
- `PLATFORM_CONTROL_DRY_RUN`: set to `true` to exercise control flows without applying real cluster changes.
- `KUBECONFIG_PATH`: kubeconfig path if testing against a real cluster.
- `PLATFORM_KUBEVIRT_INFRASTRUCTURE_PROFILE`: `AUTO`, `DEDICATED_KUBEVIRT`, or `NESTED_KUBEVIRT`.
- `TENANT_RUNTIME_BOOTSTRAP_ARTIFACT_URL`: bootstrap JAR URL used by KubeVirt guest startup.
- `TENANT_RUNTIME_BOOTSTRAP_ARTIFACT_SHA256`: optional checksum for the bootstrap artifact.

## Webstore Environment Variables

Useful storefront variables for local manual testing:

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`: storefront database connection.
- `STOREFRONT_DATABASE_SCHEMA`: storefront schema; defaults to `public`.
- `MEDIA_STORAGE_PATH`: defaults to `./media`.
- `THEME_STORAGE_PATH`: defaults to `./media/themes/active`.
- `STOREFRONT_RUNTIME_TENANT_ACCOUNT_ID`, `STOREFRONT_RUNTIME_SITE_ID`, `STOREFRONT_RUNTIME_SITE_SLUG`, `STOREFRONT_RUNTIME_PUBLICATION_VERSION`, `STOREFRONT_RUNTIME_PRIMARY_DOMAIN`: useful when simulating a tenant runtime locally.

## Platform Routes To Test

Public pages on `http://localhost:8081`:

- `GET /`: platform landing page.
- `GET /features`: platform feature page.
- `GET /pricing`: platform pricing page.
- `GET /about`: platform about page.
- `GET /login`: provider login page.

Provider entry routes:

- `GET /auth/{registrationId}`: platform login entry for a provider.
- `GET /admin/auth/{registrationId}`: admin login entry for a provider.
- `GET /login/oauth2/code/{registrationId}`: Spring Security OAuth callback target.

Authenticated platform UI:

- `GET /app`: authenticated platform shell.
- `GET /app/dashboard`: authenticated dashboard landing page.
- `GET /onboarding`: onboarding form.
- `POST /onboarding`: onboarding submission.
- `POST /logout`: logout endpoint.

Admin UI on the same app:

- `GET /admin/login`: admin login page.
- `GET /admin`: admin dashboard.
- `GET /admin/tenants`: tenant list.
- `GET /admin/sites`: site list.
- `GET /admin/sites/{siteId}`: site detail.
- `GET /admin/deployments`: deployment history.
- `GET /admin/operations`: orchestration operations view.
- `GET /admin/audit`: admin audit view.

Admin mutation routes:

- `POST /admin/sites/{siteId}/launch`
- `POST /admin/sites/{siteId}/stop`
- `POST /admin/sites/{siteId}/restart`
- `POST /admin/sites/{siteId}/destroy`
- `POST /admin/sites/{siteId}/sync`
- `POST /admin/sites/{siteId}/mark-ready`
- `POST /admin/sites/{siteId}/resources`
- `POST /admin/sites/{siteId}/infrastructure-profile`
- `POST /admin/sites/{siteId}/domain`
- `POST /admin/sites/{siteId}/publish`

Health endpoints:

- `GET /actuator/health`
- `GET /actuator/info`

## Internal Control API Routes

These routes are hosted by `platform-spring-webview` on `http://localhost:8081`, even though the logic lives in `platform-internal-api`.

Auth requirements:

- Send `X-Platform-Internal-Key: <secret>`.
- The default secret is `change-me`, but that should be overridden for any non-trivial testing.

Routes:

- `POST /internal/control/sites/{siteId}/launch`
- `POST /internal/control/sites/{siteId}/stop`
- `POST /internal/control/sites/{siteId}/restart`
- `DELETE /internal/control/sites/{siteId}`
- `PUT /internal/control/sites/{siteId}/resources`
- `PUT /internal/control/sites/{siteId}/infrastructure-profile`
- `PUT /internal/control/sites/{siteId}/domain`
- `POST /internal/control/sites/{siteId}/publications`
- `POST /internal/control/sites/{siteId}/mark-ready`
- `POST /internal/control/sites/{siteId}/sync`
- `GET /internal/control/sites/{siteId}/status`
- `GET /internal/control/sites/{siteId}/compatibility`

Example status call:

```powershell
curl.exe -H "X-Platform-Internal-Key: change-me" http://localhost:8081/internal/control/sites/<site-id>/status
```

Example compatibility call:

```powershell
curl.exe -H "X-Platform-Internal-Key: change-me" http://localhost:8081/internal/control/sites/<site-id>/compatibility
```

## Webstore Routes To Test

Storefront pages on `http://localhost:8080`:

- `GET /`: storefront home.
- `GET /products/{slug}`: product detail page.
- `GET /cart`: cart page.
- `GET /checkout`: checkout page.
- `GET /checkout/sessions/{externalReference}`: checkout result/status page.
- `GET /tracking`: tracking lookup page.
- `GET /orders/{orderNumber}/confirmation`: order confirmation page.

Storefront mutation routes:

- `POST /cart/items`
- `POST /cart/items/{lineItemId}`
- `POST /cart/items/{lineItemId}/remove`
- `POST /checkout/sessions`
- `POST /checkout/sessions/{externalReference}/simulate-success`
- `POST /tracking`
- `POST /webhooks/stripe`
- `POST /webhooks/paypal`

Storefront admin routes:

- `GET /admin`
- `GET /admin/products`
- `GET /admin/products/new`
- `GET /admin/products/{id}`
- `POST /admin/products`
- `GET /admin/offers`
- `POST /admin/offers/bundles`
- `POST /admin/offers/subscriptions`
- `GET /admin/site-settings`
- `POST /admin/site-settings`
- `GET /admin/payments`
- `POST /admin/payments`
- `GET /admin/analytics`
- `POST /admin/analytics`
- `GET /admin/pages/{slug}`
- `POST /admin/pages/sections`
- `POST /admin/pages/sections/{sectionId}/move-up`
- `POST /admin/pages/sections/{sectionId}/move-down`
- `GET /admin/media`
- `POST /admin/media`
- `GET /admin/themes`
- `POST /admin/themes/files`
- `POST /admin/themes/import`
- `GET /admin/orders`
- `GET /admin/orders/{orderNumber}`
- `POST /admin/orders/{orderNumber}/shipment`
- `GET /admin/engagement`
- `GET /admin/feature-flags`
- `POST /admin/engagement/{flagKey}`
- `POST /admin/feature-flags/{flagKey}`

Likely useful actuator check:

- `GET /actuator/health`

## Current Automated Tests

Platform internal API tests:

- `PlatformInternalApiDelegationIntegrationTest`: verifies internal control endpoints delegate into the runtime control service correctly.
- `KubeVirtClusterCompatibilityEvaluatorTest`: verifies dedicated versus nested KubeVirt compatibility decisions.
- `TenantSiteRuntimeSpecFactoryTest`: verifies runtime spec construction and selector/profile behavior.
- `TenantSiteLifecycleTransitionsTest`: verifies legal site lifecycle transitions.

Storefront tests:

- `AdminControllerIntegrationTest`: verifies the admin flow loads with the test profile and database.
- `AdminControllerRenderingTest`: verifies admin pages render.
- `CartServiceTest`: verifies cart mutations and totals.
- `ProductRepositoryTest`: verifies catalog persistence and query behavior.
- `PaymentWebhookServiceTest`: verifies payment webhook handling.
- `StorefrontControllerTest`: verifies storefront page rendering.

## Human And AI Testing Notes

- The platform boot app hosts the platform UI and the internal control API. There is no separate Boot launcher for `platform-internal-api`.
- If OAuth providers are not configured, `/login` and `/admin/login` still render, but end-to-end login cannot complete.
- Master admin access requires a provider-authenticated platform session whose email matches `PLATFORM_MASTER_ADMIN_EMAILS`.
- For internal API and KubeVirt testing, `PLATFORM_CONTROL_DRY_RUN=true` is the safest first step on a new machine.
- For real KubeVirt guest boot testing, make sure `TENANT_RUNTIME_BOOTSTRAP_ARTIFACT_URL` points at a reachable JAR and the target host can actually run the selected infrastructure profile.
- If a manual test fails, capture the endpoint, HTTP status, request body, environment variables in use, and whether the run was dry-run or cluster-backed. That is enough context for another human or AI to continue without re-discovering the basics.
