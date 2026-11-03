# Core Architecture Description

> **Alignment note (read first).** This document is structured per
> ISO/IEC/IEEE 42010 (Systems and Software Engineering — Architecture
> Description). It is an **alignment** of the core's documentation with that
> standard's concepts (stakeholders, concerns, viewpoints, views,
> correspondence rules). It is **not** a certification, **not** a compliance
> claim, and **not** an audit result. The upstream scaffold states it is a
> research scaffold, not a production certification or
> regulatory-compliance claim; that applies to this core as well. All data
> in examples is synthetic.

## 1. Purpose and scope

This description covers the generic core domain service of Open
Agriculture: the first domain service of the reference architecture
(roadmap item 1, upstream issue #1). The core provides multi-tenant
resources for agriculture and livestock — Organization, Establishment,
Parcel, CropSeason, Animal, Herd, Batch — over a versioned OpenAPI
contract, and emits CloudEvents over AsyncAPI. It covers the service
itself, its contracts, its events, its deployment on Knative Serving,
and its test and example artefacts. It does not cover the transversal
services of the scaffold (identity, consent, audit, catalog, policy,
observability) except at their interfaces with the core, and it does not
cover any production deployment, real farm data, or regulatory approval.

## 2. Stakeholders and concerns

| Stakeholder | Concerns |
| --- | --- |
| Farm / ranch operator | Can I register my operation, parcels, animals and lots without rework? Are my data isolated from other tenants? Is the API understandable? |
| Enterprise / cooperative IT | Can one core serve many companies and sites at scale? Can it integrate with existing tools through open contracts? |
| Integrator / client developer | Are the contracts stable, versioned and machine-readable? What envelope must every request and event carry? What happens on duplicate or invalid requests? |
| Platform operator | How is the service deployed, scaled to zero, probed and observed? What resources does it need? |
| Maintainer / reviewer | Are changes small, contract-plus-test together, provider-neutral and synthetic-data only? |
| Auditor / governance reviewer | Is there an audit trail (correlation, causation, tenant, purpose, classification)? Is policy enforced as code? |

## 3. Viewpoints

| Viewpoint | Concerns addressed | Model kinds used |
| --- | --- | --- |
| Functional / logical | What the core does, its entities and operations | Component + data model |
| Deployment | Where the core runs and how it scales | Container + Knative topology |
| Runtime / interaction | How requests and events flow, in order | Sequence + event flow |
| Security | How trust, isolation and audit are achieved | Trust boundary + control mapping (see `security-isms.md`) |
| Data | What the canonical data look like and how they evolve | Schema + vocabulary governance |

## 4. Views

### 4.1 Logical view

One service, seven entities, all scoped by tenant from the store up:

- **Organization** — a company; an organization IS a tenant.
- **Establishment** — a farm, ranch or plant, belonging to an organization.
- **Parcel** and **CropSeason** — agriculture: a parcel belongs to an
  establishment; a crop season runs on a parcel.
- **Animal** and **Herd** — livestock: an animal belongs to an
  establishment; a herd groups animals of one establishment.
- **Batch** — the traceability anchor: one source (a parcel or a herd)
  to one product lot.

Components inside the service: Camel routes (thin HTTP adapter),
`CoreService` (domain rules: validation, references, idempotency),
`InMemoryStore` (tenant → entity type → id), `Outbox` (local event log),
`IdempotencyStore` (tenant, entity, key → original result). The store is
deliberately isolated behind a small API so a durable implementation can
replace it without touching routes (see ADR 0002).

Operations (OpenAPI 3.1, `services/core/openapi.yaml`): per entity,
`POST /v1/{entity}` (201), `GET /v1/{entity}/{id}` (200/404),
`GET /v1/{entity}?status=` (200). Every operation requires
`X-Correlation-ID` (UUID) and `X-Tenant-ID`; optional `Idempotency-Key`
(POST), `X-Causation-ID`, `X-Purpose`, `X-Data-Classification`. Errors
are JSON bodies, never stack traces. `GET /health` is the only endpoint
without envelope headers; it carries no domain data.

### 4.2 Deployment view

The service is a single container (Quarkus JVM fast-jar, non-root user,
see `services/core/Dockerfile`), deployable identically on a laptop
(`java -jar target/quarkus-app/quarkus-run.jar`, or `mvn quarkus:dev`)
and on Knative Serving (`infrastructure/knative/core-service.yaml`:
scale-to-zero, `minScale: 0`, `maxScale: 10`, `containerConcurrency:
100`, requests 250m/256Mi, limits 1000m/1Gi, readiness/liveness probes
on `/health`). No cloud-specific annotations, no vendor extensions, no
cloud SDKs: the same artifact is provider-neutral. Knative injects
`PORT`; the application reads it (`quarkus.http.port=${PORT:8080}`).
The Knative manifest has not been applied to a cluster in this
workspace (no cluster available); it is written per the scaffold's
conventions and marked as not applied.

### 4.3 Runtime / interaction view

Synchronous request flow (consistent with `docs/architecture.md`):

1. API Gateway validates identity, quota and contract.
2. Consent and policy services authorize purpose and data use.
3. Semantic mediation maps external vocabularies to the canonical model.
4. The core service performs its local transaction: validate envelope
   headers, validate references (for example parcel → establishment,
   batch → parcel or herd), apply idempotency, persist in memory.
5. The Outbox records the corresponding CloudEvent; Inbox and
   idempotency protect consumers (consumer-side Inbox and Saga
   coordination are deployment concerns outside this service; the
   service itself emits to a local outbox log).
6. Audit and OpenTelemetry preserve end-to-end evidence (the service
   logs outbox lines; full OTel instrumentation is a follow-up — see
   `quality-requirements.md`).

Event flow: one create produces exactly one event on the existing
channel `open-agriculture.domain-events.v1` (AsyncAPI 3.0,
`application/cloudevents+json`): `OrganizationRegistered`,
`EstablishmentRegistered`, `ParcelRegistered`, `CropSeasonStarted`,
`AnimalRegistered`, `HerdFormed`, `BatchCreated`. A repeated create with
the same idempotency key returns the original result and emits no second
event (verified by test). Cross-tenant access returns 404, never a
leak: a resource owned by another tenant is indistinguishable from a
missing one. The existing `farm-observation` aggregate and its
`FarmObservationRecorded` message are untouched and remain
backwards-compatible on the same channel.

### 4.4 Security view

Trust boundary: the gateway owns identity; the core service assumes
Zero Trust at its own edge — it validates every envelope header on
every request, isolates tenants in the store, and never trusts a caller
to declare another tenant's data. See `security-isms.md` for the full
ISMS-aligned control mapping, including what is implemented today and
what is planned (real authentication/authorization live at the gateway,
not in this service).

### 4.5 Data view

Canonical model: every resource follows the upstream DomainResource
pattern (required `id`, `schema_version`, `status`, `updated_at`) plus
`tenant_id`. `schema_version` is `1.0` throughout. Every event is a
CloudEvents 1.0 JSON document carrying the envelope as extension
attributes (`tenantid`, `correlationid`, `causationid`, `schemaversion`,
`purpose`, `dataclassification`) with a JSON Schema under
`contracts/cloudevents/`. Vocabulary governance follows the scaffold:
external values map to a versioned canonical vocabulary, transformations
record source, target, rule version and provenance, and breaking changes
require a new schema version with a documented coexistence period.
No real data: all examples are synthetic (`tenant-demo`,
"Finca Ejemplo").

## 5. Architecture decisions

- **ADR 0002** — one generic multi-tenant core; contract-first
  (OpenAPI 3.1 + AsyncAPI 3.0 + CloudEvents); in-memory store behind an
  isolated API; local outbox; creates only (no updates/deletes) in this
  version; alternatives considered (Go/Python service, Spring Boot,
  Kafka-first events, relational database) with reasons for rejection.
- **ADR 0003** — the runtime of ADR 0002 is superseded: Quarkus 3.x +
  camel-quarkus replaces standalone Camel Main + Jetty. The route
  model, contracts, envelope, store and tests are unchanged; only the
  runtime, packaging and HTTP engine change (platform-http on Quarkus
  Vert.x). ADR 0002 is not rewritten; ADR 0003 references and supersedes
  only its runtime section.

## 6. Correspondence rules

These rules must hold across views; the contract tests enforce the
machine-checkable ones:

1. Every entity in the logical view has an OpenAPI schema, an AsyncAPI
   message, a CloudEvents JSON Schema, one request example and one
   event example (`ContractTest`, `validate-examples.py`).
2. Every `/v1` operation requires `X-Correlation-ID` and `X-Tenant-ID`
   (OpenAPI parameters + HTTP tests).
3. Every event type in the AsyncAPI document has a matching JSON Schema
   and a matching example whose `type` equals the message name.
4. Every resource in the data view carries `tenant_id` and
   `schema_version: 1.0`; every event carries the full envelope.
5. Every deployment artefact (Dockerfile, Knative manifest) exposes the
   same `/health` probe the runtime view documents.
6. No view introduces a cloud-specific dependency: deployment stays
   provider-neutral, and no view uses real personal data.
7. A breaking contract change requires a new `schema_version` and a
   documented coexistence period (vocabulary governance), never a silent
   edit of the existing version.
