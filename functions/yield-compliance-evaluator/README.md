# Yield Compliance Evaluator (serverless function)

Quarkus Funqy function on Knative (scale to zero). Compares measured yield gap and soil health against seasonal targets; one finding per breach.

Invoke: `POST /yieldComplianceEvaluator` with the KPI payload.
