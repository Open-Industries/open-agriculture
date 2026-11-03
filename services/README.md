# Services

One independently deployable Quarkus microservice per bounded context. Every service
follows the same hexagonal layout, exposes `/openapi`, `/q/health` and `/q/metrics`,
returns RFC 9457 problem details, and is configured only through environment variables
(Twelve-Factor). Lifecycle transitions take no body; `@Consumes` appears only on
endpoints that read one.

**Implementation order** (see `../docs/implementation-roadmap.md`):
Fields → Water Budgets → Agronomy Rules → Open APIs.

| Service | Port | Alignment |
|---|---|---|
| `field-service` | 8081 | AgGateway ADAPT field concepts |
| `irrigation-service` | 8082 | Water accounting under adopter regulation; ISO 22000 record discipline |
| `agronomy-rules-service` | 8083 | Agronomic practice encoded as auditable rules |
| `machine-service` | 8084 | ISO 11783 equipment identity (registry level) |
| `notification-service` | 8085 | Platform support (shared pattern) |

Run the pilot service:

```bash
mvn -pl services/field-service quarkus:dev
# OpenAPI UI: http://localhost:8081/docs — health: http://localhost:8081/q/health
```
