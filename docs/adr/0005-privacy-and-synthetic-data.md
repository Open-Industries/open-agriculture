# ADR-0005: Privacy by design; all reference data is synthetic

| Field | Value |
|---|---|
| Status | Accepted |
| Date | 2026-11-03 |
| Standard traceability | ISO/IEC/IEEE 42010 (architecture decision record) |

## Context

A public reference core must be safe to clone, demo and audit. ISO 27002 A.5.34.

## Decision

Farm and field data is commercially sensitive: operator identifiers are internal UUIDs, no personal data is required by any contract, and recipients are never logged. Internal keys are UUIDs and no real personal or confidential data ever enters this repository.

## Consequences

Correlation with production records happens through the adopter's governed processes, not this API.
