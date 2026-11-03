# Threat Model (STRIDE summary)

Scope: the platform boundary in `security-architecture.md`. Reviewed at every architecture change.

| Threat | Example in this core | Mitigation in place |
|---|---|---|
| **S**poofing | Forged channel credentials | Gateway OIDC; mTLS inside |
| **T**ampering | Altering a consumption record | Append-only records; balance changes only through the guarded operation; reconciliation against events |
| **R**epudiation | Operator denies a rule change | Audit log (actor, timestamp, correlation id); decisions carry rule + reason |
| **I**nformation disclosure | Personal/secret data via API or logs | Masking/minimization per the data architecture; RFC 9457 without internals |
| **D**enial of service | Event flood or API abuse | Gateway rate limiting, HPA, Knative concurrency limits |
| **E**levation of privilege | A compromised low-risk service reading another context's data | Database-per-service credentials open only its own schema |
