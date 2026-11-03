# Open Agriculture — Reference Smart Farming Core Platform

A **standard, generic agriculture and food production core platform** that any organization in this vertical
can deploy and adapt: built as **microservices and serverless functions** on
**Java 21 + Quarkus**, aligned with **The Twelve-Factor App**, documented under the ISO
architecture and quality standards, and aligned with the domain standards of the
vertical (see the traceability matrix). Everything is *aligned to* the cited standards —
**no certification, conformance or regulatory-compliance claim is made**. All data is
**synthetic**.

> Reference implementation: small, complete and runnable. The rules that matter in this
> domain are enforced in code and covered by tests — start with the invariant:
> the seasonal water budget of a field is never overdrawn — an irrigation draw that exceeds the remaining budget is rejected (422) and records nothing.

## Standards alignment

| Standard / framework | What it governs here | Where |
|---|---|---|
| ISO/IEC/IEEE **42010** | Architecture description | `docs/architecture/architecture-description.md` |
| ISO/IEC **25010** (SQuaRE) | Product quality model and scenarios | `docs/architecture/quality-attributes.md` |
| ISO/IEC/IEEE **15288 & 12207** | Life-cycle processes | `docs/lifecycle/software-lifecycle.md` |
| ISO/IEC **27001 & 27002** | Information security | `docs/security/` |
| **The Twelve-Factor App** | Cloud-native methodology | `docs/architecture/twelve-factor.md` |
| ISO 11783 (ISOBUS) | Tractor/implement data | `docs/domain/domain-standards.md` mapping |
| AgGateway ADAPT | Field operations data model | Field/machine context shapes |
| OGC SensorThings / ISO 19156 | Observations & measurements | Telemetry-facing event contracts |
| ISO 22000 | Food-safety traceability | Append-only field/season records |

Full traceability: `docs/compliance/standards-traceability.md`.

## The platform at a glance

### Microservices (Quarkus, `services/`)

| Service | Port | Alignment | Responsibility |
|---|---|---|---|
| `field-service` | 8081 | AgGateway ADAPT field concepts | Field registry: land units, crops and seasons (ADAPT-aligned shapes). |
| `irrigation-service` | 8082 | Water accounting under adopter regulation; ISO 22000 record discipline | Seasonal water budgets per field and the irrigation draws against them. |
| `agronomy-rules-service` | 8083 | Agronomic practice encoded as auditable rules | Decision rules binding crops to water and weather envelopes, with reasoned decisions. |
| `machine-service` | 8084 | ISO 11783 equipment identity (registry level) | Fleet registry: tractors, harvesters, drones and pumps. |
| `notification-service` | 8085 | Platform support (shared pattern) | Channel-agnostic dispatch of domain notifications from events. |

### Serverless functions (Quarkus Funqy + Knative, `functions/`)

| Function | Trigger | Responsibility |
|---|---|---|
| `crop-health-scorer` | Event / HTTP | Scores field risk signals (pests, drought stress, disease) into a transparent 0-100 score with rationale. |
| `yield-compliance-evaluator` | Event / HTTP | Compares measured yield gap and soil health against seasonal targets; one finding per breach. |

## Design principles

1. **One bounded context per service**, identical hexagonal layout everywhere.
2. **The domain invariant is code, not prose** — the seasonal water budget of a field is never overdrawn — an irrigation draw that exceeds the remaining budget is rejected (422) and records nothing.
3. **Privacy by design** — Farm and field data is commercially sensitive: operator identifiers are internal UUIDs, no personal data is required by any contract, and recipients are never logged.
4. **Database per service, API-first (OpenAPI), events as CloudEvents.**
5. **Adapt by configuration** — rules, thresholds, tariffs/rates and providers are data
   and configuration, not code forks (see *Adaptation guide*).

## Quick start

Prerequisites: Java 21, Maven 3.9+, optionally Docker/Podman and Knative/OpenShift.

```bash
mvn verify                                    # build and test everything
mvn -pl services/field-service quarkus:dev
docker compose up -d                          # local PostgreSQL, Kafka, Keycloak
```

## Deployment

- **Kubernetes / OpenShift**: `kubectl apply -k deploy/kubernetes/`.
- **Serverless**: Knative Services in `deploy/knative/` (scale to zero).
- **Local**: `docker-compose.yml` for dependencies; services run with `quarkus:dev`.

## Adaptation guide

| You want to… | Change only… |
|---|---|
| Adjust domain rules and thresholds | Rule data in the `agronomy-rules-service` context and function configuration |
| Change the guarded capacity semantics | The `irrigation-service` context (invariant stays: never negative) |
| Plug your IdP / providers | OIDC at the gateway; provider adapters at the boundary |
| Replace in-memory persistence | The repository port's JPA adapter on the service's own database (`DB_KIND`, `DB_URL`) |

## Documentation map

- Architecture (42010): `docs/architecture/` · Decisions: `docs/adr/`
- Domain standards: `docs/domain/domain-standards.md`
- Quality (25010), Security (27001/27002), Life cycle (15288/12207), API, Data, Operations: under `docs/`

## License

Apache-2.0 — see `LICENSE`.
