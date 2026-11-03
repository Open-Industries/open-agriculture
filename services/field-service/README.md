# Field Service

Bounded context: **Fields** — Field registry: land units, crops and seasons (ADAPT-aligned shapes).

Alignment: AgGateway ADAPT field concepts. Lifecycle transitions take no body; secret identifiers,
where the domain has them, are masked at creation and never stored or returned in clear.
All data is synthetic.

| Item | Value |
|---|---|
| Port | `8081` (`HTTP_PORT`) |
| Base path | `/api/v1/fields` |
| Owns data | Field records |
| Publishes | `field.created`, lifecycle events (see `contracts/asyncapi.yaml`) |

Run locally: `mvn -pl services/field-service quarkus:dev`.
