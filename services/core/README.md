# Open Agriculture Core Service

Generic multi-tenant core for agriculture and livestock, and the first
domain service of the Open Agriculture reference architecture. Java 21,
Quarkus 3.x + Apache Camel (camel-quarkus, platform-http), in-memory
state only. This runtime supersedes the standalone Camel Main + Jetty
runtime of ADR 0002; see ADR 0003. Routes, contracts, envelope, store
and behavior are unchanged.

## What it does

Seven entities, all scoped by tenant from the store up:
# Open Agriculture Core Service (contracts)

Generic multi-tenant core for agriculture and livestock, and the first
domain service of the Open Agriculture reference architecture.
This change defines the service contract; the Java/Camel/Knative
reference implementation follows in a follow-up PR (see issue #1).

## Entities

| Entity | Path | Event |
| --- | --- | --- |
| Organization (a tenant) | `/v1/organizations` | OrganizationRegistered |
| Establishment (farm, ranch, plant) | `/v1/establishments` | EstablishmentRegistered |
| Parcel | `/v1/parcels` | ParcelRegistered |
| CropSeason | `/v1/crop-seasons` | CropSeasonStarted |
| Animal | `/v1/animals` | AnimalRegistered |
| Herd | `/v1/herds` | HerdFormed |
| Batch (traceable lot) | `/v1/batches` | BatchCreated |

Each supports `POST` (create, 201), `GET /{id}` (200/404) and `GET`
(list, optional `?status=` filter). Full contract: `openapi.yaml` in this
directory; events: `asyncapi.yaml` here and JSON Schemas under
`../../contracts/cloudevents/`. Examples: `../../examples/core/`.

Every request must carry `X-Correlation-ID` (UUID) and `X-Tenant-ID`.
Optional: `Idempotency-Key` (POST), `X-Causation-ID`, `X-Purpose`,
`X-Data-Classification`. `GET /health` is the only unauthenticated
endpoint, for container probes.

## Build and run locally

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

Dev mode (live reload):

```bash
mvn quarkus:dev
```

The service listens on `quarkus.http.port`, which resolves as: the
`core.port` system property, then the `PORT` environment variable
(Knative injects it), then 8080. See
`src/main/resources/application.properties`.

Smoke test (replace the UUIDs with fresh ones if you replay the flow):

```bash
curl -s -X POST localhost:8080/v1/organizations \
  -H 'X-Tenant-ID: tenant-demo' \
  -H 'X-Correlation-ID: aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1' \
  -H 'Content-Type: application/json' \
  -d @../../examples/core/requests/organization.json
```

Then chain establishment → parcel → animal → herd → batch with the
returned ids (each create needs its own fresh `X-Correlation-ID` and
the same `X-Tenant-ID`). A repeated create with the same
`Idempotency-Key` returns the original resource without a second event.

## Run on Knative

Build the image (from this directory), push it to a registry, set the
image in `../../infrastructure/knative/core-service.yaml`, then:

```bash
kubectl apply -f ../../infrastructure/knative/core-service.yaml
kubectl get ksvc open-agriculture-core -n open-agriculture
```

The manifest scales to zero when idle, caps concurrency at 100, and sets
CPU/memory requests and limits. No cloud-specific annotations are used.
The Quarkus fast startup keeps scale-from-zero latency low; a native
image build is possible later without code changes but is not built
here.

## Idempotency and compensation (required by services/README)

- **Idempotency:** a repeated `POST` with the same `Idempotency-Key`,
  tenant and entity type returns the original resource (same id) instead
  of creating a duplicate. Keys are scoped per tenant. Retries without a
  key are not deduplicated; callers that retry must send a key.
- **Local transaction + outbox:** each create is one in-memory write
  followed by one CloudEvent appended to the local outbox (also logged as
  `OUTBOX <type> <id>`). Consumers must apply the upstream Inbox +
  idempotency protection; this service does not redeliver.
- **Compensation:** this service owns only its own state and publishes
  facts, never commands, so there is no distributed rollback to perform
  here. A downstream rejection is compensated by a later lifecycle
  transition on the affected resource (for example, a batch moving to
  `CLOSED`) in a future iteration that adds updates. Updates and deletes
  are deliberately out of scope for this first version (see ADR 0002).
- **Known limitation:** the store and the outbox are in-memory. A restart
  loses state and undelivered events. That is acceptable for the
  reference implementation and for scale-to-zero demos; a production
  deployment must replace `InMemoryStore`/`Outbox` with durable,
  broker-backed implementations behind the same APIs.

## Tests

```bash
mvn test
```

Covers: service rules (CRUD, envelope validation, references,
idempotency, tenant isolation, status filters, event envelope), HTTP
behavior via `@QuarkusTest` + RestAssured (status codes, headers,
cross-tenant 404, health probe), and contract tests (every synthetic
example validated against its OpenAPI/JSON Schema, plus structural
checks on both contract documents).
`X-Data-Classification`.

Validate all YAML/JSON with `tests/validate.sh` from the repository root.
