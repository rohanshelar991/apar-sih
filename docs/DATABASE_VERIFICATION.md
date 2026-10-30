# Backend database integration verification

Verified on 30 October 2026 (Asia/Calcutta), branch `feature/db-postgres`.
Status **PASS** means executed successfully; **NOT RUN** means it did not execute.
No endpoint, database password, API key, JWT secret or connection string is included here.

## 1. Supabase connection status — PASS

Connected to the approved Supabase PostgreSQL 17.11 demo with SSL required by
the JDBC configuration. Normal access authenticates as the application role;
Flyway authenticates as the separate migration role. Both credentials and the
JWT key remain in ignored `backend/.env`. Supabase API keys/JWKS are not needed
for this JDBC integration. TLS is enforced on the client-to-session-pooler leg;
the database backend's `pg_stat_ssl` view describes the separate pooler-to-server
connection and is not evidence of client-side TLS negotiation.

## 2. Flyway migration status — PASS

Flyway 10.20.1 validates V1/V2 and reports schema version **2**, with no pending
migrations. Live history has exactly two successful entries, populated checksums,
and the migration role as installer. Neither applied SQL file changed. No new
schema migration was required. SQL uses identity columns, ordinary relational
constraints and PostgreSQL partial indexes available in both PostgreSQL 16 and 17.
PostgreSQL 16 remains the AWS RDS target; execution on 16 is separately **NOT RUN**
because Docker is unavailable.

## 3. All 10 database tables verified — PASS

The two additional audited entities are `user_accounts` and `verification_requests`.
The live contract tests compare all **88 columns** with the JPA annotations and
verify all **10 foreign keys**, all primary keys, identity strategies, mapped
uniqueness, declared migration indexes and CHECK names.

| Table | Columns | Foreign-key targets | Status |
| --- | ---: | --- | --- |
| institutions | 6 | — | PASS |
| programmes | 7 | institutions | PASS |
| students | 15 | institutions, programmes | PASS |
| upload_batches | 10 | institutions | PASS |
| validation_issues | 12 | students, upload_batches | PASS |
| student_history | 7 | students | PASS |
| verification_tokens | 8 | students | PASS |
| audit_logs | 9 | — | PASS |
| user_accounts | 8 | institutions | PASS |
| verification_requests | 6 | verification_tokens | PASS |

Types, VARCHAR lengths, nullability and exact column sets match. IDs use BIGINT
generated-by-default identity, matching JPA `GenerationType.IDENTITY`.
Timestamps remain without time zone; Hibernate and database sessions use UTC.
Audit details/history remain VARCHAR. Defaults are initialized by the existing
Java entities and services; no SQL column DEFAULT expressions were invented.
Every current non-ID column has no database default; identity columns use their
identity mechanism. The schema contract test verifies this distinction.

## 4. Hibernate validation result — PASS

The postgres profile has `ddl-auto=validate` and the PostgreSQL dialect.
EntityManagerFactory initialized without missing tables, missing columns, type
mismatches or relationship errors. Independent live catalog comparisons cover
details Hibernate's built-in validation does not fully check. Flyway remains
the schema authority; the postgres profile does not create/update/drop tables.

## 5. Spring Boot startup result — PASS

Started a real embedded Tomcat HTTP server on an ephemeral port under the
postgres profile. Flyway and Hibernate validated; Spring Security initialized;
Swagger, authenticated directory/programme/institution/audit requests, and the
unauthenticated 401 check succeeded over HTTP. The test JVM shuts the server down.

Windows initially failed in Java NIO native loopback handling with a shortened
temp path. Setting session-local TEMP/TMP to a normal absolute directory fixed
startup; the exact workaround is in [DATABASE.md](DATABASE.md). An IPv4-only
flag was tried and did not fix this environment issue.

## 6. Student CRUD test results — PASS

JWT-authenticated POST creates a VALID student; list with and without search,
fetch-by-ID, update and CSV export succeed. Update creates field history and an
audit record. Service tests validate malformed APAAR values and institution-wide
duplicates across programmes. Live PostgreSQL enforces the partial unique index,
accepts non-counted ERROR/DUPLICATE rows and rejects counted collisions. The
cross-institution report excludes other admission years. Orphan foreign keys
are rejected with SQLSTATE 23503.

There is no student DELETE API in the existing application. Deletion of a separate,
unreferenced temporary fixture passed through its existing Spring Data repository.
No new delete endpoint or deletion policy was introduced. All write tests rolled back.

## 7. Institution CRUD test results — PASS

Created, listed, fetched, updated and safely deleted an unreferenced temporary
institution through the existing repository; authenticated institution-list API
requests passed. There is no institution write API to exercise. No existing
institution or demo record was deleted.

## 8. Programme CRUD test results — PASS

Created, fetched by institution, updated intake and deleted temporary programmes
through the existing repository. The programme API returns its expected nested
institution relationship. Orphan creation fails. There is no programme write API;
no new endpoints were added.

## 9. Application-role permission results — PASS

Live checks verify SELECT/INSERT/UPDATE/DELETE grants on every mutable application
table and SELECT/INSERT-only access to audit/history. Actual UPDATE/DELETE on
append-only tables, TRUNCATE attempts and public-schema CREATE fail with SQLSTATE
42501. The role cannot read Flyway history, does not own schema objects, and has
no superuser, create-role or create-database capability. Its lack of schema DDL
and migration-history rights prevents it managing Flyway migrations.
This checks application-schema DDL; it does not revoke PostgreSQL's separate
default permission to create session-local temporary objects.

## 10. Migration-role permission results — PASS

Flyway validates/migrates using the migration identity. The migration role owns
all 10 application tables and Flyway history. CREATE TABLE and ALTER TABLE were
executed successfully inside a transaction and rolled back. No probe table remains.
Applied migration checksums were not repaired, baselined or rewritten.

## 11. Backend Maven verification — PASS

| Executed command / suite | Passed | Skipped | Failed/errors | Status |
| --- | ---: | ---: | ---: | --- |
| `mvn -f "backend/pom.xml" verify` | 21 | 17 | 0 | PASS |
| Opt-in `External*Test` against Supabase | 13 | 0 | 0 | PASS |
| Original ValidationEngineTest + IntakeMonitoringTest | 12 | 0 | 0 | PASS |
| PostgreSQL 16 Testcontainers execution | 0 | 4 | 0 | NOT RUN |

The ordinary verify run compiles, tests, packages and repackages the executable
Spring Boot JAR. Its 17 skips are the 13 deliberately opt-in live tests (executed
separately above) plus four Docker-dependent tests. No skipped test is counted as
a pass. H2 startup/seeding, repository tests, filter regressions and profile/TLS
guards pass. Live suites include service, controller, real HTTP, schema and role tests.

Two PostgreSQL-only failures were reproduced and fixed: absent audit actor filters
and absent student search filters bound nulls as binary values, causing
`lower(bytea)` errors. Criteria predicates now omit absent filters while preserving
filter semantics, sorting, pagination and export behavior. H2 and live regressions pass.

## 12. Frontend build and compatibility — PASS

`npm ci --no-audit --no-fund` and `npm run build` succeeded using the lockfile.
TypeScript compilation and Vite production output passed. Vite reports a
non-fatal chunk-size advisory; no frontend redesign was performed.

The existing Axios base URL is relative `/api`. Vite proxies it to the local
backend in development. Production hosting must reverse-proxy `/api` to the
backend; the frontend currently has no VITE API-base variable. Login still sends
email/password and receives token/tokenType plus user/institution fields. Student,
programme and institution DTO structures are unchanged. Axios retains its existing
401 login redirection and rejected-error handling. Backend errors 400/401/403 and
allowed/disallowed CORS origins were executed successfully. A browser UI walkthrough
is **NOT RUN**; the frontend build and API compatibility checks do not claim one.

## 13. Security and Git verification — PASS; rotation pending

- Current app password, migration password and JWT key, and Supabase secret-key
  patterns were absent from tracked/unignored files, Git text history and current
  verification logs. The private env file remains ignored. This is a targeted
  check of supplied/generated credentials and recognizable Supabase secret keys,
  not a claim of a universal secret audit.
- Both root and backend env examples contain placeholders for secrets. Root's
  obsolete sample password/key values were replaced. `.env.*` is now ignored,
  with `.env.example` retained as the explicit exception.
- The unused default Spring Security user auto-configuration is excluded so it
  cannot generate/log a fallback password. Existing JWT authentication remains
  verified by a successful login/API test. SQL filters use parameterized criteria.
- JWT key material stays server-side. Verification tokens remain random opaque
  database tokens; a verification-signing secret is not consumed by this design.
- CORS permits configured exact origins and rejects the untrusted test origin.
  No Supabase secret key was found in frontend source or tracked build inputs.
- The provisioning password and API secret previously shared in chat must be
  rotated. Their rotation/revocation status is **NOT RUN**, and no keys are repeated
  here. Newly generated app/migration/JWT credentials are separate from them.

## 14. Files changed or created

Production fixes:

- `backend/src/main/java/com/infobro/apaar/repository/AuditLogRepository.java`
- `backend/src/main/java/com/infobro/apaar/repository/StudentRepository.java`
- `backend/src/main/java/com/infobro/apaar/ApaarConnectApplication.java`
- `.gitignore` and `.env.example`

Verification tests:

- `backend/src/test/java/com/infobro/apaar/AuditLogRepositoryTest.java` (new)
- `backend/src/test/java/com/infobro/apaar/StudentRepositoryTest.java` (extended)
- `backend/src/test/java/com/infobro/apaar/ExternalBackendApiTest.java` (new)
- `backend/src/test/java/com/infobro/apaar/ExternalSchemaContractTest.java` (new)
- `backend/src/test/java/com/infobro/apaar/ExternalHttpStartupTest.java` (new)

Documentation: `docs/DATABASE.md` and this report. Applied migrations, JPA entity
fields, rule codes, controller endpoint signatures and frontend source are unchanged.
Local helper tools, private env values, test logs and build outputs are ignored.

## 15. Remaining errors or pending tasks

- No unresolved failure remains in the executed verification suites.
- **NOT RUN:** PostgreSQL 16 Testcontainers checks, requiring a Docker-enabled
  machine/CI before AWS production deployment. Live 17.11 success is not a 16 run.
- **NOT RUN:** AWS provisioning/deployment and a real browser UI walkthrough.
- **NOT RUN:** rotation of previously exposed provisioning/API credentials; do
  that through the provider dashboard, without pasting replacements into chat.
- Set the deployed frontend origin and production reverse proxy when hosting.
  Production-profile demo seeding remains off. Real user-account provisioning is
  separate; tests did not leave login accounts in the database.
- Read-only post-test verification found all 10 application tables free of test
  and seed records. No existing user/demo data was deleted. Identity sequence
  counters can advance during rolled-back tests, which is normal.

Reproduction commands and environment loading are documented in [DATABASE.md](DATABASE.md).
