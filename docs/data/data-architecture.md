# Data Architecture

Data view (VP-5) of the Architecture Description.

## 1. Ownership

| Data | Owner (only writer) | Store (reference → production) |
|---|---|---|
| Fields records | field-service | in-memory → PostgreSQL `field` |
| Water Budgets records | irrigation-service | in-memory → PostgreSQL `irrigation` |
| Agronomy Rules records | agronomy-rules-service | in-memory → PostgreSQL `agronomy` |
| Machines records | machine-service | in-memory → PostgreSQL `machine` |
| Notifications records | notification-service | in-memory → PostgreSQL `notification` |

In-memory adapters exist so the platform runs anywhere with zero setup; production
persistence is configuration (`DB_KIND`, `DB_URL`) plus the JPA adapter — the domain and
contracts never change (12-Factor IV).

## 2. Privacy and identifiers

Farm and field data is commercially sensitive: operator identifiers are internal UUIDs, no personal data is required by any contract, and recipients are never logged. Internal keys are UUIDs. All values in this repository are synthetic.

## 3. Consistency, evidence, retention

Strong within a context; eventual across contexts via CloudEvents with idempotent
consumers. Consumption and decision records are append-only evidence; corrections are
compensating records, never edits. Retention per the adopter's regulatory schedule;
personal data erasable per data-subject rights while evidential records are retained in
pseudonymized form (UUIDs only). Backups per service, encrypted, restore tested
(ISO 27002 A.8.13).
