# Core Information Security Management (ISMS alignment)

> **Non-certification disclaimer — read first.** This document is
> structured per ISO/IEC 27001 (ISMS requirements) and ISO/IEC 27002
> (controls). It is an **alignment** with those standards. It is **not**
> a certification, **not** a claim of compliance, and **not** the result
> of an audit. No certification body has assessed this core. The
> upstream scaffold is a research scaffold, not a production
> certification or regulatory-compliance claim, and this core inherits
> that status. Controls marked "implemented today" describe the current
> code and configuration only; controls marked "planned" are not yet
> implemented and must not be read as present.

## 1. Scope statement

The ISMS scope for this core covers: the core service code
(`services/core/`), its contracts (`services/core/openapi.yaml`,
`services/core/asyncapi.yaml`), its event schemas
(`contracts/cloudevents/`), its examples (`examples/core/`), its
deployment artefacts (`services/core/Dockerfile`,
`infrastructure/knative/core-service.yaml`), and its documentation.
Out of scope: the transversal scaffold services (identity, consent,
audit, catalog, policy, observability) except at their interfaces;
any production deployment, registry, cluster, or broker; any real farm
or personal data (the core ships synthetic data only, and real data
must not be introduced without an approved privacy and governance
process, per the scaffold).

## 2. Risk-assessment approach (qualitative)

**Assets:** tenant domain data (organizations, establishments,
parcels, animals, herds, batches); the event stream (outbox log);
the container image; the source repository and its history; the
contracts themselves (a changed contract changes every consumer).

**Threats (illustrative, not exhaustive):** cross-tenant data access;
malformed or oversized payloads; duplicate or replayed creates;
spoofed events; vulnerable dependencies; compromised or mistagged
container images; accidental inclusion of real personal data in
examples or tests; misconfigured deployment (probes, resources, scale).

**Vulnerabilities known today (stated, not hidden):** the store is
in-memory (data loss on restart, no access control inside the store —
isolation relies on the tenant key discipline); authentication and
authorization are assumed at the gateway and not enforced in this
service; the Knative manifest carries a placeholder image (a real
deployment must pin a digest); the outbox is a local log, not a
central audit pipeline; OpenTelemetry instrumentation is not yet in
the service.

**Risk criteria (qualitative):** likelihood (rare / possible / likely)
× impact (minor / material / severe), judged per asset. A risk is
acceptable only when a control below addresses it or a maintainer
records why it is accepted. Cross-tenant leakage and real-data
inclusion are never acceptable; both are treated as severe impact
regardless of likelihood.

## 3. Control mapping (ISO/IEC 27002:2022 Annex A)

Status key: **Implemented** = in the code/configuration today.
**Planned** = not yet implemented. **N/A** = not applicable to this
scope, with the reason given. "Gateway" means the scaffold's API
Gateway / consent / policy services, which own that control outside
this service.

### 5. Organizational controls

| Control | Status | How / justification |
| --- | --- | --- |
| 5.1 Policies for information security | Implemented (partial) | This document + `architecture-core.md` + ADRs state the security posture; a full policy set is a maintainer follow-up. |
| 5.2 Information security roles and responsibilities | Implemented (partial) | Maintainers review; contributors follow CONTRIBUTING (issue first, small PRs, contracts + tests together). |
| 5.3 Segregation of duties | N/A | No operational roles exist in the scaffold; production segregation is a deployment responsibility. |
| 5.15 Access control (policy) | Planned | Policy-as-code (Rego) exists in the scaffold (`policies/`); core-specific policies are not yet written. |
| 5.16 Identity management | Gateway | Identity is validated at the gateway per the scaffold flow; the service trusts the gateway and validates the tenant header. |
| 5.17 Authentication information | Gateway | No credentials are handled by this service; none are stored in the repo. |
| 5.19 Supplier relationships | N/A | No suppliers; dependencies are open-source libraries (see 8.9, 8.34). |
| 5.28 Collection of evidence | Implemented (partial) | Outbox log lines per create, carrying tenant, correlation, causation, purpose, classification. Central collection is planned. |
| 5.31 Legal and contractual requirements | N/A (note) | No legal advice is given; licence is Apache-2.0; real-data use requires an approved governance process (scaffold rule). |
| 5.35 Independent review | Implemented (partial) | Maintainer PR review is the review mechanism; no independent audit has occurred. |

### 6. People controls

| Control | Status | How / justification |
| --- | --- | --- |
| 6.1 Screening | N/A | Open-source contributors are not screened; review gates substitute. |
| 6.2 Terms and conditions of employment | N/A | No employees in scope. |
| 6.3 Information security awareness | Implemented (partial) | CONTRIBUTING + this document state the rules (synthetic data only, no secrets in the repo). |
| 6.4 Disciplinary process | N/A | Community moderation, not an employment process. |
| 6.5 Responsibilities after termination | N/A | No access is granted by this service itself (gateway-owned). |

### 7. Physical controls

| Control | Status | How / justification |
| --- | --- | --- |
| 7.1–7.4 Physical security, entry, equipment | N/A | No premises, no equipment in scope; physical security belongs to the hosting provider of any real deployment. |
| 7.5–7.14 (media, cabling, maintenance, disposal) | N/A | No physical media or equipment in scope. |

### 8. Technological controls

| Control | Status | How / justification |
| --- | --- | --- |
| 8.2 Privileged access rights | Implemented | Least privilege by design: the service needs no credentials, no database, no cloud APIs; the container runs as a non-root user. |
| 8.3 Access restriction | Implemented | Tenant isolation in the store (tenant, entity, id); cross-tenant reads return 404 (tested). |
| 8.5 Secure authentication | Gateway | Authentication is enforced at the gateway; the service validates envelope headers on every request. |
| 8.9 Configuration management | Implemented | Versioned contracts; breaking changes require a new `schema_version` + coexistence period; manifests are declarative. |
| 8.10 Delete of information | Planned | No delete operation exists yet (creates only); a delete/retention process must accompany it. |
| 8.11 Data masking | N/A (note) | No real personal data exists in scope; masking becomes applicable the day real data is introduced. |
| 8.12 Data leakage prevention | Implemented (partial) | Cross-tenant 404s (no oracle), JSON error bodies without stack traces, synthetic examples only. |
| 8.13 Backup | Planned | In-memory store has no backup; a durable store must define backup before production use. |
| 8.15 Logging | Implemented (partial) | Outbox log per create + error JSON bodies; structured audit logging and OTel are planned. |
| 8.16 Monitoring activities | Planned | `/health` probe exists (Knative readiness/liveness); metrics, alerting and OTel collection are planned. |
| 8.17 Clock synchronization | N/A (note) | Timestamps use the host clock; a deployment must synchronize clocks (platform responsibility). |
| 8.20 Network security | Gateway + planned | Gateway owns network policy; the service binds 0.0.0.0 inside its container and relies on the platform for network segmentation. |
| 8.24 Cryptography | Planned | No cryptography is implemented in this service; TLS termination is a platform/gateway responsibility. |
| 8.26 Technical vulnerability management | Planned | Dependencies are pinned (Quarkus/Camel BOMs); no vulnerability scanning is wired into the repo yet. |
| 8.34 Audit testing of systems | Implemented (partial) | Contract tests, HTTP tests and example validation run on every change; no security-specific test suite exists yet. |

## 4. Incident, logging and monitoring notes

- **Logging today:** outbox lines (`OUTBOX <Event> <id>`) per unique
  create; JSON error bodies for rejected requests; no request bodies
  are logged (bodies may contain business data and are not needed for
  the audit trail — the event carries the record).
- **Monitoring today:** `GET /health` returns `{"status": "UP"}` and
  backs the Knative readiness/liveness probes. No metrics endpoint,
  no alerting, no OTel exporter is wired into the service yet; the
  scaffold's `observability/` collector is the intended sink.
- **Incident handling (proposed, not exercised):** on suspected
  cross-tenant leakage or real-data inclusion: freeze the affected
  contract version, preserve the outbox log, rotate any exposed
  credentials at the gateway, and disclose through the upstream issue
  tracker. No incident has occurred; no incident process has been
  exercised, and this paragraph is a proposal, not a tested procedure.
- **What would change this document's status:** a production
  deployment with real tenants, a durable store, gateway authentication
  enforced, central audit collection, OTel instrumentation, dependency
  scanning, and an independent review. Until then, every "planned"
  above stays planned.
