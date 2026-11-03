# Core Quality Requirements (SQuaRE alignment)

> **Alignment note (read first).** This document is structured per
> ISO/IEC 25010 (Systems and software Quality Requirements and
> Evaluation, SQuaRE). It is an **alignment** with that standard's
> product-quality model. It is **not** a certification, **not** a
> compliance claim, and **not** an audit result. Each requirement below
> states whether it is **demonstrated** (by the current implementation
> and its tests, with evidence) or a **target** (not yet measured or not
> yet implemented). Do not read targets as achieved results.

## How to read this document

- **ID** — stable requirement identifier (QR-01 …).
- **Measure / threshold** — the concrete, testable bar.
- **Evaluation** — how it is checked: `test` (automated test),
  `inspection` (review of an artefact), or `metric` (measured value).
- **Status** — `demonstrated` or `target`.

## 1. Functional suitability

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-01 | All seven core entities support create, get-by-id and list | 7/7 entities × 3 operations, per `openapi.yaml` | test (`ContractTest.openapiStructure`) | demonstrated |
| QR-02 | Every `/v1` operation rejects a missing or non-UUID `X-Correlation-ID` | 400 with a JSON error body, no stack trace | test (`CoreRouteHttpTest`) | demonstrated |
| QR-03 | Every `/v1` operation rejects a missing `X-Tenant-ID` | 400 with a JSON error body | test (`CoreRouteHttpTest`) | demonstrated |
| QR-04 | A create with a repeated idempotency key returns the original resource and emits no second event | Same id on repeat; outbox count unchanged | test (`CoreRouteHttpTest`, smoke chain) | demonstrated |
| QR-05 | Cross-tenant access is indistinguishable from a missing resource | 404 on get, empty list on list, for another tenant's id | test (`CoreRouteHttpTest`) | demonstrated |
| QR-06 | Every create emits exactly one CloudEvent of the documented type on the documented channel | 1 event per unique create; type matches AsyncAPI | test (outbox log, smoke chain) | demonstrated |
| QR-07 | Unknown entity types are rejected | 400 for an unknown `{entity}` path segment | test (`CoreRouteHttpTest`) | demonstrated |
| QR-08 | Creates validate references (parcel → establishment, batch → parcel or herd) | 400/404 when the referenced resource does not exist in the tenant | test (`CoreServiceTest`) | demonstrated |

## 2. Performance efficiency

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-09 | Container resource envelope fits small-farm and scaled deployments | Requests 250m/256Mi, limits 1000m/1Gi (Knative manifest) | inspection | demonstrated (declared) |
| QR-10 | Service starts fast enough for Knative scale-to-zero to be useful | Cold start to first 200 on `/health` in under 10 s on the declared envelope | metric | target (not measured on a cluster) |
| QR-11 | Steady-state request latency stays interactive | p95 under 200 ms for create/get/list on the declared envelope | metric | target (not measured under load) |
| QR-12 | Throughput scales horizontally with Knative replicas | No shared mutable state between replicas beyond the (replaceable) store | inspection | target (in-memory store is per-replica; a durable store is required before horizontal scale-out is a real claim) |

## 3. Compatibility

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-13 | Synchronous contract is OpenAPI 3.1.0 and stable | `openapi: 3.1.0`; no breaking change without a new `schema_version` | test (`ContractTest`) | demonstrated |
| QR-14 | Events are AsyncAPI 3.0.0 + CloudEvents 1.0 JSON on the existing channel | Channel `open-agriculture.domain-events.v1`, `application/cloudevents+json` | test (`ContractTest`) | demonstrated |
| QR-15 | Every shipped example validates against its schema | 14/14 examples (7 requests + 7 events) | test (`validate-examples.py`) | demonstrated |
| QR-16 | Backwards compatibility with the existing aggregate | `farm-observation` / `FarmObservationRecorded` unchanged and still valid on the same channel | inspection | demonstrated |

## 4. Interaction capability

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-17 | Errors are machine-readable and never leak internals | JSON `{"error": ...}` body; no stack traces on any tested error path | test (`CoreRouteHttpTest`) | demonstrated |
| QR-18 | The API is self-describing | OpenAPI + AsyncAPI documents plus a README with curl examples for every operation | inspection | demonstrated |
| QR-19 | Clients can discover entity lifecycles | Status vocabularies documented in `docs/domain-model-core.md` (for example ACTIVE/SUSPENDED, ALIVE, CREATED) | inspection | demonstrated |

## 5. Reliability

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-20 | Duplicate delivery does not duplicate effects | Same idempotency key → same result, no second event (see QR-04) | test | demonstrated |
| QR-21 | A failed create leaves no partial entity | Validation precedes persistence; failed requests return 4xx with no stored resource | test (`CoreServiceTest`) | demonstrated |
| QR-22 | Data survives restarts | Durable store replacing the in-memory store; restart test with data intact | test | target (in-memory store loses data on restart — documented limitation, not a hidden one) |
| QR-23 | The service recovers from a crash without manual repair | Knative restarts the container; readiness probe gates traffic until `/health` is 200 | inspection | target (probes declared; not exercised on a cluster) |

## 6. Security

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-24 | Tenant isolation is enforced in the store, not only at the edge | Store keys are (tenant, entity, id); cross-tenant reads return 404 | test (`CoreRouteHttpTest`, `CoreServiceTest`) | demonstrated |
| QR-25 | No real personal data is shipped or required | All examples synthetic; no PII fields in any schema | inspection | demonstrated |
| QR-26 | Authentication and authorization are enforced | Gateway-issued identity verified on every request; unauthorized callers rejected | test | target (assumed at the gateway per the scaffold; not implemented in this service) |
| QR-27 | Audit evidence exists for every mutation | Outbox event per create, carrying tenant, correlation, causation, purpose, classification | test (outbox log) | demonstrated (local log; central audit pipeline is a follow-up) |

## 7. Maintainability

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-28 | The codebase is modular: routes, rules, store and events are separable | One package per concern; store behind a small API; routes contain no domain rules | inspection | demonstrated |
| QR-29 | Decisions are recorded | ADR 0002 (core) and ADR 0003 (Quarkus runtime) document context, decision, alternatives, trade-offs | inspection | demonstrated |
| QR-30 | Contracts and tests change together | No contract change without updated examples and passing `validate-examples.py` + `ContractTest` | test (CI gate to be enforced by reviewers) | demonstrated (process; enforced by review, not yet by CI) |

## 8. Flexibility

| ID | Requirement | Measure / threshold | Evaluation | Status |
| --- | --- | --- | --- | --- |
| QR-31 | The service runs without a specific cloud | No cloud SDKs, no cloud annotations; same container on laptop and Knative | inspection | demonstrated |
| QR-32 | The runtime is replaceable | Quarkus JVM image today; native image possible without code changes (Quarkus native-ready) | inspection | target (native image not built) |
| QR-33 | The store is replaceable | Store API isolates persistence; a durable implementation swaps in without touching routes | inspection | demonstrated (by design; replacement not yet built) |

## 9. Quality in use (note)

For farm and ranch operators, quality in use means: **effectiveness** —
registering an operation, parcel, animal or lot succeeds the first time
without rework (supported today by strict, explicit validation errors);
**efficiency** — the common flows are one POST each (supported today);
**satisfaction** — the API is predictable across all seven entities
(same envelope, same error shape, same idempotency rule — supported
today); **freedom from risk** — a tenant can never see another tenant's
data, and a retried request never duplicates a lot (tested today);
**context coverage** — the same core serves a single farm and a
multi-site enterprise (designed for it via multi-tenancy; large-scale
operation is a target until a durable store and load measurements
exist). No operator study has been run; these are design notes, not
measured user outcomes.
