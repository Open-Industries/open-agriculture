# API Standards

Binding for every public interface (ADR-0007).

## REST

- Resources, not verbs; lifecycle transitions as sub-resources (`POST /api/v1/<ctx>/{id}/<verb>`).
- **Transitions take no body and must not require a Content-Type** — `@Consumes` belongs
  only on endpoints that actually read a body (a rule written after a real CI failure in
  a sister core).
- URI major versioning (`/api/v1`); breaking changes ship a new major with a deprecation window.
- Codes: create `201`; async dispatch `202`; validation `400`; domain rule `422`;
  illegal transition `409`; not found `404`.
- Errors: RFC 9457 `application/problem+json` — never stack traces or internals.
- **Idempotency**: every create accepts `Idempotency-Key`; retries return the original result.
- Money/quantities: explicit unit fields with named units; timestamps RFC 3339 UTC;
  ids are UUIDs; every call carries `X-Correlation-Id`.

## Events

CloudEvents 1.0 envelope; type naming `<context>.<event>` in past tense; additive-only
evolution within a major version; consumers idempotent and tolerant of unknown fields;
ordering per aggregate (`subject`). Catalogue: `contracts/asyncapi.yaml` (CR-3).
