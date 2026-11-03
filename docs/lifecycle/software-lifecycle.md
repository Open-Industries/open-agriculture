# Software Life-Cycle Processes — ISO/IEC/IEEE 15288 & 12207

The platform's way of working mapped to the life-cycle processes of ISO/IEC/IEEE 15288
(system) and ISO/IEC/IEEE 12207 (software).

## 1. Life-cycle model and gates

| Gate | Stage | Exit evidence |
|---|---|---|
| G0 | Concept | Business need; affected contexts identified |
| G1 | Architecture | ADR added/updated; threat-model delta reviewed (42010) |
| G2 | Build | Contract-first implementation; unit + contract tests; Twelve-Factor checklist |
| G3 | Verify | `mvn verify` green in CI; QS-* scenarios re-run for touched contexts; scans clean |
| G4 | Release | Immutable image promoted; release notes; rollback plan |
| G5 | Operate | SLOs monitored; incidents recorded; lessons feed G0 |

## 2. Process mapping (15288/12207, summary)

Business/mission analysis (backlog, G0) · Stakeholder requirements (AD concerns; 25010
scenarios as measurable requirements) · Supply (versioned releases of this repository) ·
Planning/decision/risk (ADRs; threat model) · Configuration management (git for code,
contracts, docs, manifests) · Measurement (metrics, SLOs, CI evidence) · Architecture
definition (42010 AD + ADRs) · Implementation (12207; Java/Quarkus modules) ·
Integration/verification (contract tests; `mvn verify`) · Transition (image promotion) ·
Validation (QS-01…QS-08 per release) · Operation/maintenance (runbook, SLO alerts; ADRs
supersede — never silently edit — decisions) · Disposal (retention/erasure per the data
architecture; context retirement via ADR).

## 3. Records per release

Release notes, CI run link, test/scan reports, promoted image digests, ADRs merged, SLO
snapshot — objective evidence for ISO 12207 information items and ISO 27001 clause 9.
