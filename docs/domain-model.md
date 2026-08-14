# Domain model

## Aggregate

- Name: `farm-observation`
- Primary event: `FarmObservationRecorded`
- Required metadata: identifier, schema version, tenant, purpose, classification, correlation and causation.

## Semantic governance

All external values are mapped to a versioned canonical vocabulary. Transformations record source, target, rule version and provenance. Breaking changes require a new schema version and a documented coexistence period.

## Data boundary

Examples are synthetic. Real data must not be introduced without approved privacy, retention, residency and access policies.
