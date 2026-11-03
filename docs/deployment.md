# Deployment (summary)

Deployment view (VP-6): see the Architecture Description and `deploy/`.

- Microservices: `kubectl apply -k deploy/kubernetes/`.
- Functions: `kubectl apply -f deploy/knative/` (scale to zero).
- Local dependencies: `docker compose up -d`.
- Immutable images promoted across identical-topology environments (12-Factor V & X).
