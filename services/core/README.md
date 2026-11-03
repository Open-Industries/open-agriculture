# Open Agriculture Core Service (contracts)

Generic multi-tenant core for agriculture and livestock, and the first
domain service of the Open Agriculture reference architecture.
This change defines the service contract; the Java/Camel/Knative
reference implementation follows in a follow-up PR (see issue #1).

## Entities

| Entity | Path | Event |
| --- | --- | --- |
| Organization (a tenant) | `/v1/organizations` | OrganizationRegistered |
| Establishment (farm, ranch, plant) | `/v1/establishments` | EstablishmentRegistered |
| Parcel | `/v1/parcels` | ParcelRegistered |
| CropSeason | `/v1/crop-seasons` | CropSeasonStarted |
| Animal | `/v1/animals` | AnimalRegistered |
| Herd | `/v1/herds` | HerdFormed |
| Batch (traceable lot) | `/v1/batches` | BatchCreated |

Each supports `POST` (create, 201), `GET /{id}` (200/404) and `GET`
(list, optional `?status=` filter). Full contract: `openapi.yaml` in this
directory; events: `asyncapi.yaml` here and JSON Schemas under
`../../contracts/cloudevents/`. Examples: `../../examples/core/`.

Every request must carry `X-Correlation-ID` (UUID) and `X-Tenant-ID`.
Optional: `Idempotency-Key` (POST), `X-Causation-ID`, `X-Purpose`,
`X-Data-Classification`.

Validate all YAML/JSON with `tests/validate.sh` from the repository root.
