# Notification Service

Bounded context: **Notifications** — shared platform pattern. Channel-agnostic dispatch
(EMAIL/SMS/PUSH) driven by domain events; providers are outbound adapters selected by
configuration. Example templates in this core: IRRIGATION_ALERT, CROP_HEALTH_WARNING, SEASON_REPORT.

| Item | Value |
|---|---|
| Port | `8085` (`HTTP_PORT`) |
| Base path | `/api/v1/notifications` |
| Owns data | Notification requests and delivery trail |
