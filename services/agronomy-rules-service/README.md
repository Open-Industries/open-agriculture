# Agronomy Rules Service

Bounded context: **Agronomy Rules** — Decision rules binding crops to water and weather envelopes, with reasoned decisions.

Alignment: Agronomic practice encoded as auditable rules. Decisions are `ALLOWED` / `DEGRADED` / `DENIED`, always
with the matched rule and a human-readable reason for audit. All data is synthetic.

| Item | Value |
|---|---|
| Port | `8083` (`HTTP_PORT`) |
| Base path | `/api/v1/agronomy-rules` |
| Owns data | AgronomyRule rules |
| Publishes | `agronomy.activated` |

Run locally: `mvn -pl services/agronomy-rules-service quarkus:dev`.
