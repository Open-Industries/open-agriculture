# ADR-0002: Java 21 and Quarkus as the single implementation stack

| Field | Value |
|---|---|
| Status | Accepted |
| Date | 2026-11-03 |
| Standard traceability | ISO/IEC/IEEE 42010 (architecture decision record) |

## Context

Type safety, a mature ecosystem, fast startup and low memory for always-on services and scale-to-zero functions; one skill set for adopting organizations.

## Decision

All services and functions are Java 21 on Quarkus 3, built by one Maven reactor.

## Consequences

Shared conventions across the platform.
