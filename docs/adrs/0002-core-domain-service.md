# ADR 0002 — Core domain service (first domain service)

Status: proposed.

## Context

The Open Agriculture reference scaffold defines an interoperability
envelope (correlation/causation ids, schema version, tenant, purpose,
data classification), governed API entry, CloudEvents/AsyncAPI
interoperability, and one aggregate (`farm-observation` /
`FarmObservationRecorded`). `services/` holds only a README, and the
roadmap's first item is to implement the first domain service. Issue #1
in the upstream repository proposes exactly this core.

Any company — from a single farm to a multi-site enterprise — must be
able to adopt the core without rework, and the same code must run on a
laptop and on serverless infrastructure.

## Decision

Build one generic, multi-tenant core domain service with seven entities:

- Organization (a company; an organization IS a tenant)
- Establishment (farm, ranch, plant)
- Parcel and CropSeason (agriculture)
- Animal and Herd (livestock)
- Batch (the traceability anchor: one source, parcel or herd, to one product lot)

Contract-first: OpenAPI 3.1 for the synchronous API and AsyncAPI 3.0 +
CloudEvents JSON Schemas for events, extending the upstream
DomainResource pattern (required id, schema_version, status, updated_at)
with tenant_id. Reference implementation in Java 21 with Apache Camel
(Camel Main + Jetty; platform-http was tried first and dropped because it
has no HTTP engine in standalone Camel Main), in-memory state, a local
outbox, and a Knative Serving manifest.

## Why Java / Camel / Knative

- The user explicitly chose this stack: Java everywhere, microservices,
  serverless Knative, OpenAPI, Apache Camel.
- Camel's route model maps naturally onto the upstream architecture
  (gateway validation -> domain transaction -> Outbox -> CloudEvents),
  and Camel Main runs without Spring or an application server, which
  keeps the container small and the deployment provider-neutral.
- Knative gives scale-to-zero for a service that is idle most of the
  time on small farms, while the same artifact scales horizontally for
  large enterprises. Nothing in the code or manifests names a cloud
  provider.

## Alternatives considered

- **Go or Python service:** smaller binaries/faster starts, but the user
  chose Java, and Camel's enterprise-integration patterns (idempotent
  consumer, outbox, dead-letter handling) match the roadmap's Saga/CQRS/
  Outbox items better than hand-rolled equivalents.
- **Spring Boot:** richer ecosystem, but heavier for a first reference
  service and unnecessary when Camel Main already provides the runtime.
- **Kafka-first events:** a broker is the production path, but mandating
  one would break provider neutrality and make the reference
  implementation unrunnable without infrastructure. The local outbox
  keeps the event contract real (events are built, logged and testable)
  while the broker binding stays a deployment decision.
- **Relational database:** durable state is a production requirement,
  but it would add a provisioned dependency to a scaffold whose
  `validate` step only checks JSON. The store is isolated behind a small
  API so a durable implementation can replace it without touching routes.

## Trade-offs and consequences

- Creates only: updates and deletes are out of scope. Lifecycle changes
  (e.g. a season moving PLANNED -> ACTIVE) arrive in a follow-up change
  with their own events and compensation rules.
- In-memory state: restarts lose data and undelivered events. Documented
  in the service README as a known limitation, not hidden.
- The existing `farm-observation` aggregate and `FarmObservationRecorded`
  message are untouched; core events are additive on the same channel.
- Every request and event carries the full envelope, which costs a few
  headers/fields per call but buys traceability, tenant isolation and
  audit for free.

## Deliberately out of scope

Authentication/authorization (the gateway owns identity, consent and
policy per the upstream architecture); billing; weather/sensor ingestion
(the existing farm-observation aggregate covers observations); UI;
migrations; any real or personal data (all examples are synthetic).
