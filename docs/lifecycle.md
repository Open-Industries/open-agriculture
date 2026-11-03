# Core Life Cycle (15288 / 12207 alignment)

> **Alignment note (read first).** This document is structured per
> ISO/IEC/IEEE 15288 (system life cycle processes) and ISO/IEC/IEEE 12207
> (software life cycle processes). It is an **alignment** of the core
> project's actual workflow with those standards' process models. It is
> **not** a certification, **not** a compliance claim, and **not** an
> audit result. The upstream scaffold is a research scaffold, not a
> production certification or regulatory-compliance claim.

## 1. Verification vs validation (as the standards define them)

- **Verification** asks: *did we build the product right?* — does the
  output of each stage satisfy the requirements set in the previous
  stage? In this project: contract tests, unit tests, HTTP tests,
  schema validation and the syntax gate.
- **Validation** asks: *did we build the right product?* — does the
  product satisfy stakeholder needs in its intended environment? In this
  project: the smoke chain against the running service, reviewer
  judgement that the core fits the scaffold's roadmap, and (in future)
  operator trials on real operations. Validation against real farm
  operations has not happened; the core ships synthetic data only.

Keeping the two apart matters: all 23 unit/HTTP/contract tests passing
is verification, not proof that farmers will find the core useful.

## 2. Processes

### 2.1 Agreement processes (15288)

- **Acquisition / supply.** The "acquirer" is the open-source community
  around the upstream scaffold; the "supplier" is this core's
  contributors. The agreement is the repository's licence (Apache-2.0)
  plus its CONTRIBUTING rules (issue before significant changes, small
  PRs, contracts and tests together, synthetic data only,
  provider-neutral). No commercial agreement exists.

### 2.2 Organizational project-enabling processes (15288)

- **Life cycle model management.** This document is the model: stages
  in §3, processes in §2, mapping to the repo workflow in §4.
- **Infrastructure management.** Toolchain: Java 21 LTS, Maven 3.9,
  Quarkus 3.x + camel-quarkus, Knative Serving manifests, container
  images. The toolchain is documented in the service README so a new
  contributor can reproduce the build.
- **Portfolio / project management.** Work is tracked as upstream
  issues (for example issue #1 proposes the core) and pull requests
  (PR #2 is phase 1: contracts, events, tests, docs). No separate
  project plan exists; the issue tracker is the plan.
- **Human resource management.** Not applicable beyond the usual
  open-source norm: reviewers, not managers, gate quality.
- **Quality management.** Review gates: every PR needs contracts and
  tests together, passing validation, and maintainer review. See
  `quality-requirements.md`.

### 2.3 Technical management processes (15288)

- **Project planning / assessment / control.** Scope is set per issue
  and per PR phase (phase 1: contracts; phase 2: reference
  implementation). Progress is visible as merged PRs.
- **Decision management.** Architectural decisions are recorded as
  ADRs (0002: core; 0003: Quarkus runtime superseding the runtime
  section of 0002). Alternatives and trade-offs are part of each ADR.
- **Risk management.** Risks are tracked qualitatively in
  `security-isms.md` (risk approach) and in ADRs (for example the
  in-memory store as a known limitation, not a hidden risk).
- **Configuration management.** Versioned contracts
  (`schema_version: 1.0`); breaking changes require a new schema
  version with a documented coexistence period. Git history on the fork
  and upstream is the configuration record; the five co-author trailers
  are a contributor convention, not a standard.
- **Information management.** Docs live with the code (`docs/`,
  `services/core/README.md`); ADRs are the durable decision record.
- **Measurement.** Test counts (23 unit/HTTP/contract tests; 14/14
  example validations), manifest resource declarations, and declared
  (not yet measured) performance targets in
  `quality-requirements.md`.

### 2.4 Technical processes (15288 system / 12207 software)

| Process | What it means for the core |
| --- | --- |
| Stakeholder needs definition | Upstream roadmap item 1 (first domain service) + issue #1 (generic multi-tenant core for agriculture and livestock). |
| System / software requirements definition | `services/core/openapi.yaml`, `services/core/asyncapi.yaml`, `contracts/cloudevents/*.schema.json`, and the envelope rules in `architecture-core.md`. |
| Architecture definition | `docs/architecture-core.md` (42010-aligned) + ADRs 0002/0003. |
| Design definition | `docs/domain-model-core.md` (entities, relationships, lifecycles) + route/store/outbox design in the service README. |
| Implementation | Java 21 + Quarkus + Camel routes; in-memory store; local outbox. |
| Integration | Contracts compose with the existing `farm-observation` aggregate on the same channel; no existing file is modified by phase 1. |
| Verification | `mvn test` (unit + HTTP + contract), `tests/validate.sh`, `tests/validate-examples.py`. |
| Validation | Smoke chain against the running jar (organization → establishment → parcel → animal → herd → batch); maintainer review; future operator trials. |
| Transition | Merge to upstream main; container image published by the deployment (placeholder in the manifest until a registry is chosen). |
| Operation | Run on Knative (scale-to-zero) or locally; `/health` probe; outbox logs as the audit trail. |
| Maintenance | Follow-up PRs (phase 2: reference implementation; later: durable store, OpenShift overlays, livestock event detail, traceability queries). |
| Disposal | Retire a schema version only after its documented coexistence period; remove deprecated endpoints in a new major contract version, never silently. |

## 3. Stages and entry/exit criteria

| Stage | Entry | Exit |
| --- | --- | --- |
| Concept | A need is stated (roadmap item, issue) | Issue accepted; scope and phasing agreed (issue #1) |
| Development | Contract draft + tests exist | `mvn test` green, examples validate, review approved, PR merged |
| Production | PR merged; image built from the merged code | Image published to a registry; manifest points at a real image (today the manifest still carries a placeholder — production is **not** reached) |
| Utilization | Service runs on Knative or locally for real tenants | Operators use it; incidents handled per `security-isms.md` |
| Support | Issues arrive from operators | Fixes land as small PRs with tests; schema versions coexist |
| Retirement | A replacement contract version is live | Old version removed only after its coexistence period ends |

## 4. Mapping: standard process → this repo's workflow

| Life cycle process | Repo workflow step |
| --- | --- |
| Stakeholder needs / requirements | Upstream issue (for example #1) |
| Architecture / design | ADR + `docs/` (reviewed in the PR) |
| Implementation | Branch on the contributor fork |
| Verification | `mvn test`, `validate.sh`, `validate-examples.py` (run before pushing) |
| Review (technical management) | Pull request review by maintainers |
| Transition / release | Merge to upstream `main`; image build (future) |
| Operation / maintenance | Follow-up issues and PRs; schema coexistence on breaking change |
| Disposal | Removal of a superseded schema version after coexistence |

## 5. What is deliberately not claimed

No stage beyond Development has been reached by this core: there is no
published image, no cluster deployment, no production tenant and no
operator validation. The Knative manifest is written, not applied; the
Dockerfile is written, not built (no Docker verified here); the native
image is not built. Those are future stages, not current achievements.
