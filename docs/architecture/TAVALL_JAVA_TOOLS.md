# Webstore Tavall Java Tools Contract

Webstore is a Tavall-owned Java consumer. Tavall DI is the universal first-party composition/lifecycle baseline across Java subprojects.

Use Tavall Database for shared PostgreSQL/JPA infrastructure, Tavall Registry for typed keyed catalogs, Tavall Concurrency for asynchronous work, Tavall Logging for runtime/application diagnostics, Tavall Cache for bounded projections, Tavall EventBus for generic typed in-process events, Tavall Reflection for reusable scanning/metadata behavior, and Tavall Scheduler for recurring/timed Java work.

Spring remains the web/application framework. Existing Spring Data/JPA wiring is migration debt where it duplicates Tavall Database infrastructure; domain repositories/entities may remain product-owned while persistence infrastructure converges on Tavall Database.

Do not add new project-local service locators, first-party ServiceLoader composition, executor frameworks, logging wrappers, registry/cache/event-bus frameworks, reflection scanners, scheduled executors, or database infrastructure for concerns already owned by the Tavall tools.

Exact Java 25 repository verification, dependency locks, web acceptance, and persistence integration tests are required before promotion.