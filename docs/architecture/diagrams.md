# Architecture Diagrams

Mermaid views governed by the viewpoints of `architecture-description.md` (ISO/IEC/IEEE 42010).

## D-1 — System Context (C4 Level 1)

```mermaid
flowchart TB
    USERS[Users / operators] --> CORE[Open Agriculture — Reference Smart Farming Core Platform]
    SYSTEMS[External systems of the vertical] --> CORE
    CORE --> IDP[Identity provider - OIDC]
    CORE --> NOTIF[Notification providers]
```

## D-2 — Containers (C4 Level 2)

```mermaid
flowchart LR
    GW[API Gateway] --> R[field-service :8081]
    GW --> O[irrigation-service :8082]
    GW --> E[agronomy-rules-service :8083]
    GW --> C[machine-service :8084]
    O -. events .-> F1[crop-health-scorer - function]
    E -. events .-> F2[yield-compliance-evaluator - function]
    R -. events .-> NT[notification-service :8085]
    O -. events .-> NT
    R --> DB1[(db: field)]
    O --> DB2[(db: irrigation)]
    E --> DB3[(db: agronomy)]
    C --> DB4[(db: machine)]
    NT --> DB5[(db: notification)]
```

## D-3 — Deployment

```mermaid
flowchart TB
    subgraph NS[Kubernetes namespace]
        SVC[Services: Deployments + HPA, probes, limits]
        FN[Functions: Knative, scale to zero]
        DBS[(PostgreSQL per service)]
        SVC --> DBS
    end
    GIT[Git - this repository] --> CICD[CI/CD: build once, promote the same image]
    CICD --> NS
```

## D-4 — Core flow (sequence)

```mermaid
sequenceDiagram
    participant U as User / channel
    participant R as field-service
    participant E as agronomy-rules-service
    participant O as irrigation-service
    U->>R: create the primary record
    U->>E: evaluate a request against active rules
    E-->>U: ALLOWED / DEGRADED / DENIED + reason
    U->>O: draw from the guarded account
    O-->>U: record + remaining (or 422 if it would overdraw)
```
