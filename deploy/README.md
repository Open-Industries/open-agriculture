# Deployment

| Path | Target |
|---|---|
| `kubernetes/` | Always-on microservices: `kubectl apply -k deploy/kubernetes/` |
| `knative/` | Serverless functions (scale to zero) |

Same manifests and images in every environment — only configuration changes
(12-Factor V & X).
