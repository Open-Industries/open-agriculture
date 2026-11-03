# Domain Standards — Agriculture

Alignment mapping for this core. *Aligned to* throughout: no certification or
conformance claim is made against any standard below.

## Concept mapping

| Platform concept | Domain standard | Mapping |
|---|---|---|
| Field, crop, season | AgGateway ADAPT / ISOXML concepts | Registry context shapes and identifiers |
| Machine telemetry & task data | ISO 11783 (ISOBUS) | Machines are registry data here; certified in-cab/implement control stays in the equipment |
| Sensor observations | OGC SensorThings API, ISO 19156 (O&M) | Observation-shaped events in `contracts/asyncapi.yaml` |
| Field/season history | ISO 22000 traceability principles | Append-only records; corrections are compensating records |
| Water accounting | (Adopter regulation) | The water-budget invariant is the platform's own hard rule |

## Scope boundaries

This core does **not** control machinery (autosteer, implement actuation) — functional
safety for agricultural machinery (e.g. ISO 25119) belongs to certified equipment. The
platform plans, budgets, decides and records; equipment executes.
