# Domain model — core service (addition to docs/domain-model.md)

This document extends the upstream domain model (whose only aggregate so
far is `farm-observation`) with the core service's seven entities. It does
not replace `docs/domain-model.md`; when this material is submitted
upstream it should be merged into it.

## Entities and relationships

```
Organization (1) ──< Establishment (N) ──< Parcel (N) ──< CropSeason (N)
                  └─< Animal (N)
                  └─< Herd (N) ── references Animal (N)
Parcel (1) ──< Batch (N)   (source_type = PARCEL)
Herd   (1) ──< Batch (N)   (source_type = HERD)
```

| Entity | Meaning | Key fields | Lifecycle |
| --- | --- | --- | --- |
| Organization | A company; an organization IS a tenant | name, country | ACTIVE, SUSPENDED |
| Establishment | A site: farm, ranch or plant | organization_id, kind, location, area_ha | ACTIVE, INACTIVE |
| Parcel | Delimited agricultural land | establishment_id, area_ha, soil_type | ACTIVE, INACTIVE |
| CropSeason | One crop on one parcel in one season | parcel_id, crop, season, planned dates | PLANNED, ACTIVE, HARVESTED, CLOSED |
| Animal | One individually identified animal | establishment_id, species, breed, sex, birth_date, tag | ALIVE, SOLD, DECEASED |
| Herd | A managed group of animals of one species | establishment_id, species, animal_ids | ACTIVE, DISSOLVED |
| Batch | A traceable product lot | source_type (PARCEL/HERD), source_id, product, quantity, unit | CREATED, TRANSFORMED, SHIPPED, CLOSED |

## Rules

- **Multi-tenancy:** every entity carries `tenant_id`. Reads, writes and
  references are scoped to the caller's tenant; a cross-tenant read
  behaves exactly like a missing resource (404), never a 403 that would
  leak existence.
- **Envelope:** every resource carries id, tenant_id, schema_version
  (`1.0`), status, created_at and updated_at (the upstream
  DomainResource pattern plus tenant_id). Every event is a CloudEvents
  1.0 JSON document with the envelope as extension attributes
  (tenantid, correlationid, causationid, schemaversion, purpose,
  dataclassification) and the resource as `data`.
- **References:** an establishment must belong to an existing
  organization; parcels, animals and herds to an existing establishment;
  crop seasons to an existing parcel; herds may only list existing
  animals; a batch's source must exist and match its source_type. All
  checks are same-tenant.
- **Traceability:** a batch points to exactly one source. Splitting or
  merging lots is a future change that must preserve this chain (a child
  batch referencing a parent batch), not break it.
- **Idempotency:** creates accept an `Idempotency-Key`; a replay returns
  the original resource. This is what makes client retries and the
  upstream Outbox/Inbox pattern safe.
- **Synthetic data only:** all examples use invented names, places and
  identifiers (for example "Finca Ejemplo", tenant "tenant-demo").
  Real data must not be introduced without an approved privacy,
  retention, residency and access process.
