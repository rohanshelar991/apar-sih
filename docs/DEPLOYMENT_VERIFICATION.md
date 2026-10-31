# Deployment verification status

No Vercel/Render deployment or new-project connection has been authorized or
performed. No live cloud URLs, migration success, new-project accounts or live
persona logins are claimed. No old-project records were changed during this work.

| Check | Actual status / evidence |
| --- | --- |
| Backend test suite after security changes | PASS: 59 discovered, 42 passed, 17 skipped, zero failures/errors |
| New exact-project guard | PASS: six offline guard tests; wrong project and transaction pooler rejected |
| Cross-institution HTTP authorization regression | PASS in disposable test database |
| Consent withdrawal, legacy token rejection and re-consent behavior | PASS in disposable test database |
| Three Spring Boot BCrypt/JWT login roles | PASS in disposable test database, not a Supabase login claim |
| JWT expiry, missing/disabled/role-changed accounts | PASS in disposable test database |
| Health endpoint and configured/untrusted CORS preflight | PASS in disposable test database |
| Frontend TypeScript and production build | PASS with compile-only HTTPS `.invalid` API URL |
| JSON/YAML configuration parsing | PASS locally |
| GitHub workflow execution | NOT RUN; configuration prepared only |
| Docker image build and PostgreSQL 16 Testcontainers | NOT RUN locally; Docker unavailable |
| Vercel public URL, SPA refresh/navigation and asset checks | NOT RUN; no deployment |
| Render public health/cold start/PORT binding | NOT RUN on Render; controller/config tested locally |
| New Supabase connection/TLS/project identity | NOT RUN; explicit authorization pending |
| New-project Flyway and all 10 Hibernate mappings | NOT RUN; explicit authorization and credentials pending |
| New-project account provisioning | NOT RUN; prohibited without separate approval |
| Three real hosted Supabase persona logins/JWTs/permissions/audit rows | NOT RUN |
| Hosted dashboard data, CSV processing, corrections, reports and QR verification | NOT RUN |
| Hosted data persistence across backend restart | NOT RUN |
| Notebook inspection | PASS; Isolation Forest export found, no backend inference integration |
| Live ML predictions and parity/load tests | NOT RUN |

The 17 default skips are 13 opt-in external-database tests and four Docker
integration tests. Later opt-in tests that write transactional fixtures must
not be run against either Supabase project without explicit authorization.
Required cloud and account setup is documented in [deployment instructions](DEPLOYMENT.md)
and [the exact Render variable guide](RENDER_ENVIRONMENT.md).
