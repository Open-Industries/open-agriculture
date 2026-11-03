# Operations Runbook

## Deploy (per environment)

1. Confirm release tag and image digests in the release record.
2. `kubectl apply -k deploy/kubernetes/`; functions: `kubectl apply -f deploy/knative/`.
3. Verify: Deployments ready, `/q/health/ready` green via the gateway, SLO dashboard nominal.
4. Rollback: re-apply the previous manifests (images are immutable, promoted, never rebuilt).

## Health endpoints (every service)

Liveness `GET /q/health/live` · Readiness `GET /q/health/ready` · Metrics `GET /q/metrics` ·
API docs `/docs` · Contract `/openapi`.

## Common incidents

| Symptom | First checks | Action |
|---|---|---|
| Decisions all DENIED | Rules context: are rules ACTIVE for the scope? | Activate or correct the rule; decisions carry their reason |
| Operations rejected with 422 | Account balance vs requested draw | Expected behavior for over-draw; replenish or correct the source — never edit balances by hand |
| Function silent | Broker delivery metrics; event types vs `contracts/asyncapi.yaml` | Restore the event feed; functions are stateless |

## Admin processes (12-Factor XII)

Rule loads, migrations, re-evaluations and key purges run as one-off Jobs or function
invocations with the same image and configuration — never as interactive sessions inside
production pods.
