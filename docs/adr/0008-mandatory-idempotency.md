# ADR-0008: Idempotency is mandatory on state-creating operations

| Field | Value |
|---|---|
| Status | Accepted |
| Date | 2026-11-03 |
| Standard traceability | ISO/IEC/IEEE 42010 (architecture decision record) |

## Context

Clients and networks retry; a duplicated record or double draw is a dispute. QS-02 depends on this.

## Decision

Creates accept an Idempotency-Key; event consumers are idempotent. Retries are always safe.

## Consequences

Safe retries end to end; services store keys with bounded retention.
