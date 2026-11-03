# Irrigation Service

Bounded context: **Water Budgets** — Seasonal water budgets per field and the irrigation draws against them.

Alignment: Water accounting under adopter regulation; ISO 22000 record discipline. Core invariant, enforced in code and tests: **the guarded
balance/capacity never goes negative** — over-drawing operations are rejected with `422`
and nothing is recorded. All data is synthetic.

| Item | Value |
|---|---|
| Port | `8082` (`HTTP_PORT`) |
| Base path | `/api/v1/water-budgets` |
| Owns data | WaterBudget accounts, WaterUse records |
| Publishes | `irrigation.consumed`, `irrigation.insufficient` |

Run locally: `mvn -pl services/irrigation-service quarkus:dev`.
