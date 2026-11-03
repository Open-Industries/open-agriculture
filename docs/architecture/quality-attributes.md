# Product Quality Model — ISO/IEC 25010 (SQuaRE)

View VP-8. The ISO/IEC 25010 product quality model applied to this platform, with
measurable scenarios.

| 25010 characteristic | Platform commitment |
|---|---|
| Functional suitability | The domain invariant holds under every API path (tested); decisions always explain themselves |
| Performance efficiency | p95 < 300 ms reads, < 800 ms writes at reference sizing (2 replicas, 500 mCPU) |
| Compatibility | OpenAPI/AsyncAPI contracts; CloudEvents envelopes; domain-standard shapes (see domain doc) |
| Usability (APIs as UI) | One error model (RFC 9457), one idempotency convention, reasoned decisions |
| Reliability | SLO 99.9% monthly per service (`../operations/slos.md`); idempotent retries; health probes |
| Security | ISO 27002-mapped controls (`../security/`); secrets never in code or logs |
| Maintainability | One context per service; identical hexagonal layout; ADRs; tests per module |
| Portability | Twelve-Factor compliance; containers run on any Kubernetes/OpenShift |

## Quality-attribute scenarios

| ID | Scenario | Target |
|---|---|---|
| QS-01 | An operation would overdraw the guarded account | Rejected `422`; balance and records unchanged (tested) |
| QS-02 | A client retries a create with the same idempotency key after a timeout | Exactly one record exists |
| QS-03 | A request arrives for a scope with no active rule | Decision `DENIED` with reason — never a silent default (tested) |
| QS-04 | An auditor asks why a decision was made | The decision names rule, priority and reason |
| QS-05 | Clean-machine clone | `mvn verify` runs the whole suite with no external services |
| QS-06 | A pod dies mid-operation | The operation is atomic per request: fully applied or fully rejected |
| QS-07 | A new domain rule/threshold is introduced | Configuration/data change in the rules context; no code changes elsewhere |
| QS-08 | Any API response or log line is inspected for secrets/PII | Farm and field data is commercially sensitive: operator identifiers are internal UUIDs, no personal data is required by any contract, and recipients are never logged. |
