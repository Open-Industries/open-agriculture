# Machine Service

Bounded context: **Machines** — Fleet registry: tractors, harvesters, drones and pumps.

Alignment: ISO 11783 equipment identity (registry level). Lifecycle transitions take no body. All data is synthetic.

| Item | Value |
|---|---|
| Port | `8084` (`HTTP_PORT`) |
| Base path | `/api/v1/machines` |
| Owns data | Machine records |

Run locally: `mvn -pl services/machine-service quarkus:dev`.
