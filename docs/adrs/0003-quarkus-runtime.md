# ADR 0003 — Quarkus runtime for the core service

Status: proposed.

## Context

ADR 0002 decided the core (seven multi-tenant entities, contract-first
OpenAPI 3.1 + AsyncAPI 3.0 + CloudEvents, in-memory store, local outbox)
with a standalone Camel Main + Jetty runtime. The user then required an
executable skeleton of the first service on the chosen stack: Java,
microservices, serverless Knative, OpenAPI, Camel — and, specifically,
a Quarkus skeleton. This ADR records the runtime change. It references
ADR 0002 and supersedes **only its runtime section**; ADR 0002 itself is
not rewritten, and its domain, contract, store and testing decisions
stand unchanged.

## Decision

Run the core service on **Quarkus 3.15.x + camel-quarkus 3.15.0**:

- Routes stay Apache Camel (`CoreRoutes`, same processors, same
  behavior), now discovered as a CDI bean (`@ApplicationScoped`) and
  served by the Quarkus HTTP server via `platform-http` (Vert.x).
- No standalone `main()` bootstrap: Quarkus owns startup, configuration
  and the HTTP server. Port resolution keeps the old precedence via
  `quarkus.http.port=${core.port:${PORT:8080}}` (system property, then
  Knative-injected `PORT`, then 8080).
- Packaging is the Quarkus fast-jar (`target/quarkus-app/quarkus-run.jar`);
  dev mode (`mvn quarkus:dev`) replaces the old run-from-jar loop.
- The `/health` probe stays a Camel route (`{"status": "UP"}`), so the
  Knative manifest's probes are unchanged; SmallRye Health is
  deliberately not introduced, to keep probe behavior identical.
- Tests move to Quarkus style where they touch HTTP: `@QuarkusTest` +
  RestAssured for `CoreRouteHttpTest` (plus a new `/health` case).
  `CoreServiceTest` and `ContractTest` are plain JUnit 5 and unchanged.

## Why Quarkus supersedes the standalone runtime

- **Executable skeleton, container-first.** Quarkus is the standard way
  to ship a Camel service as a container: one build produces the
  runnable fast-jar, the Dockerfile copies a fixed layout, and dev mode
  gives live reload. The standalone shade jar required manual manifest
  merging (ServicesResourceTransformer) for Camel component discovery.
- **Fast startup for Knative scale-to-zero.** The manifest scales to
  zero when idle; every scale-from-zero event pays the startup cost.
  Quarkus starts the same routes measurably faster than a standalone
  Camel Main + Jetty process and uses less memory at rest, which is
  exactly the cost profile a small farm (idle most of the time) and a
  large enterprise (bursty load) both need.
- **Native-ready without code changes.** A GraalVM native image can be
  built later from the same code; the standalone runtime had no such
  path. The native image is not built here (no GraalVM verified in this
  workspace) — this is an option, not a claim.
- **Platform-http works here.** The standalone runtime dropped
  platform-http because Camel Main standalone has no HTTP engine for
  it (verified by test failure 2026-11-03). Under camel-quarkus,
  platform-http is backed by the Quarkus Vert.x server, so the original
  design (Camel REST DSL on platform-http) is restored.

## What stays the same

Contracts (`openapi.yaml`, `asyncapi.yaml`, CloudEvents schemas),
examples (byte-identical), envelope validation, idempotency,
tenant isolation, the outbox event log, the in-memory store, the
Knative scale-to-zero posture, provider neutrality (no cloud SDKs,
no cloud annotations), and the synthetic-data-only rule.

## Alternatives considered

- **Keep standalone Camel Main + Jetty.** Zero migration cost, but no
  dev mode, slower cold starts on Knative, and manual fat-jar
  assembly. Rejected: it fights the scale-to-zero goal.
- **Spring Boot + Camel.** Richer ecosystem, but heavier for a first
  reference service, and the user chose Quarkus explicitly.
- **Quarkus + RESTEasy instead of Camel routes.** More idiomatic for
  plain REST, but it would discard the Camel route model the user
  chose (and ADR 0002's integration-pattern reasoning: idempotent
  consumer, outbox, dead-letter handling map to Camel patterns).
- **SmallRye Health for /health.** More idiomatic Quarkus, but it
  moves the probe to `/q/health` and changes the Knative manifest for
  no behavioral gain. Rejected for this iteration; revisitable when
  metrics/tracing land.

## Trade-offs and consequences

- Build complexity moves from the shade plugin to the Quarkus Maven
  plugin (augmentation step); contributors need a Quarkus-compatible
  JDK (21 here) and the Quarkus + camel-quarkus BOM pair, which must
  be kept mutually compatible (pinned in `pom.xml`, verified by the
  test run).
- The service now depends on the Quarkus runtime (CDI, Vert.x). That
  is a framework commitment, not a cloud commitment: the container
  still runs anywhere, and no cloud SDK is introduced.
- Quarkus dev mode and the fast-jar layout change the Dockerfile and
  the run commands (see the service README); the old
  `target/open-agriculture-core-0.1.0.jar` path no longer exists.
- Unchanged limitations carry over: in-memory store and outbox (data
  loss on restart), no real authentication in the service (gateway
  assumption), Dockerfile not built here (no Docker verified),
  Knative manifest not applied (no cluster), native image not built.
