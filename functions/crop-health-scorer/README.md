# Crop Health Scorer (serverless function)

Quarkus Funqy function on Knative (scale to zero). Scores field risk signals (pests, drought stress, disease) into a transparent 0-100 score with rationale.

Invoke: `POST /cropHealthScorer` with the event payload.
