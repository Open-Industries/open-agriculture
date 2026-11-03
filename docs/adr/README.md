# Architecture Decision Records (ADRs)

Part of the Architecture Description (ISO/IEC/IEEE 42010 §5.6). Accepted ADRs are
immutable; a change is a new ADR that supersedes the old one.

| ADR | Title |
|---|---|
| [0001](0001-microservices-and-serverless.md) | Microservices per bounded context, complemented by serverless functions |
| [0002](0002-java-quarkus-stack.md) | Java 21 and Quarkus as the single implementation stack |
| [0003](0003-database-per-service.md) | Database per service; no shared database, no distributed transactions |
| [0004](0004-domain-invariant.md) | The domain invariant is enforced in code |
| [0005](0005-privacy-and-synthetic-data.md) | Privacy by design; all reference data is synthetic |
| [0006](0006-domain-standards-alignment.md) | Domain-standards alignment as the organizing principle |
| [0007](0007-api-first-contracts.md) | API-first contracts with RFC 9457 problem details and CloudEvents |
| [0008](0008-mandatory-idempotency.md) | Idempotency is mandatory on state-creating operations |
| [0009](0009-security-model.md) | Security model: OIDC at the edge, mTLS inside, platform-managed secrets |
| [0010](0010-twelve-factor-binding.md) | Twelve-Factor alignment as a binding application standard |
