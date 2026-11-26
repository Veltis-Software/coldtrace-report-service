# ColdTrace Report Service

Java 21 and Spring Boot service for asynchronous organization-scoped traceability reports (US14). Readings arrive through `readings.ingested`; the service owns its MySQL read model and hourly aggregates. Report requests snapshot incidents, assignees and corrective actions through the Alert API, then generate a consolidated JSON result in a background job.

## API

- `POST /api/v1/reports`: title, periodStart and periodEnd; responds 202 with job ID.
- `GET /api/v1/reports/{id}`: job status.
- `GET /api/v1/reports/{id}/result`: completed report; 409 while pending.

All report endpoints require a valid IAM bearer session with `reports:view`. Queries and results are scoped to that session's organization. Periods are start-inclusive and end-exclusive, with a maximum of 90 days.

## Development

Install the shared library from the sibling `coldtrace-shared` repository with `mvn -B -f ../coldtrace-shared/pom.xml install`, then run `mvn verify` here. The build runs the unit tests and the Cucumber acceptance suite in `src/test/resources/features`. Use the infrastructure repository's Compose configuration to run MySQL, the Pub/Sub emulator and the other services together. The emulator push endpoint is disabled by default and enabled only in that local configuration.

Database migrations live in `src/main/resources/db/migration`. Duplicate events and reading IDs do not inflate aggregates. Tests cover duplicate delivery, transaction rollback, report consolidation and organization isolation.

## Deployment

The service runs in Google Cloud Run as `coldtrace-report`, with its `ct_report` schema in Cloud SQL and an authenticated Pub/Sub push subscription. See `CLOUD_DEPLOYMENT.md` in `coldtrace-infrastructure`.
