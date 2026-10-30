# Database operations

## Modes

| Profile | Schema management | Demo data | TLS |
| --- | --- | --- | --- |
| Default (`h2`) | In-memory H2, Hibernate `create-drop`; Flyway disabled | On unless `SEED_DEMO_DATA=false` | Embedded database |
| `postgres` | Flyway V1/V2, then Hibernate `validate` | Always off, even if the flag is true | `DB_URL` must contain exactly one `sslmode=require` |
| `postgres,dev` | Same migrations and validation | Only if `SEED_DEMO_DATA=true` | Explicit local development exemption |

`h2,postgres` does not enable the seeder. Never activate `dev` in production.
The default demo still requires no external database. Its data disappears when
the application stops. H2 compatibility mode does not emulate PostgreSQL partial
indexes, roles, grants, or every PostgreSQL behavior.

## Schema and migration policy

- `V1__baseline_schema.sql` creates **10** entity-aligned tables: institutions,
  programmes, students, upload_batches, validation_issues, student_history,
  verification_tokens, audit_logs, user_accounts, verification_requests.
- `V2__indexes_and_constraints.sql` adds the existing index access paths, the
  requested composite indexes, institution/token uniqueness, and a partial
  unique student index on `(apaar_id, admission_year, institution_id)` for
  statuses `VALID`, `PENDING`, `VERIFIED`. Unique constraints replace redundant
  indexes on institution code, token, and user email.
- Students have no active flag. “Active” for this index means those three
  counted statuses, matching the intake logic. `ERROR` and `DUPLICATE` records
  remain storable. The same-institution validation rule now ignores programme
  and queries only counted records. Cross-institution reporting filters both
  inner and outer queries by admission year.
- CHECK values are sourced from `ValidationEngine` (categories and counted
  statuses), `ValidationResult` (validation statuses), `ValidationIssueInfo`
  (severity), `ValidationService`/`IntakeService` (issue statuses), and
  `UploadService` (batch statuses). These are string values in Java, not Java
  enums. Unconstrained strings such as institution status and verification
  request status do not acquire guessed enum domains.
- Timestamps remain `TIMESTAMP WITHOUT TIME ZONE`, matching `LocalDateTime`.
  Hibernate JDBC and PostgreSQL connections use UTC. Run the production JVM
  with `-Duser.timezone=UTC` as well: `LocalDateTime.now()` still uses JVM time.
  A move to TIMESTAMPTZ requires a separate migration and an explicit conversion
  policy for existing local timestamps.
- Audit details/history values remain VARCHAR because current values are plain
  strings, not guaranteed JSON. `audit_logs.entity_id` remains VARCHAR(64).
- **Never edit a migration after application.** Add `V3__...sql` and subsequent
  versions. Flyway validates checksums and maintains `flyway_schema_history`.
  `baseline-on-migrate=false` prevents silently adopting an unmanaged schema.
  Do not enable `ddl-auto=update`, Flyway clean, or automatic baselining in production.

### Existing data and demo audit

The Java seeder generates distinct sequential APAAR IDs plus one separate
cross-institution example, so it has no counted same-institution collision.
`sample-uploads/new_admissions_INST001.csv` intentionally repeats APAAR
`202699110022` for 2026. The later duplicate receives `DUPLICATE` before saving
and is outside the partial index predicate. The template has no repeated key.
The samples use valid category values. Invalid enum-like values submitted by
other clients are rejected by the PostgreSQL CHECK constraints.

For any existing PostgreSQL data, inspect collisions before attempting conversion:

```sql
SELECT apaar_id, admission_year, institution_id, count(*)
FROM students
WHERE verification_status IN ('VALID', 'PENDING', 'VERIFIED')
GROUP BY apaar_id, admission_year, institution_id
HAVING count(*) > 1;
```

These migrations target a **fresh application database**. An existing database
created by Hibernate `update` needs a separately reviewed export/import or
schema reconciliation plan; do not run V1 over it or force a baseline.

## Environment

Copy `backend/.env.example` to `backend/.env` and replace placeholders privately.
The copy is ignored by Git. Spring Boot does **not** load `.env` automatically;
export variables, configure your IDE, or inject them from your deployment platform.
Do not include passwords in command-line arguments, URLs, logs, or commits.

| Variable | Meaning / default |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Unset for H2; `postgres` for hosted DB; `postgres,dev` for local DB |
| `DB_URL` | Required PostgreSQL JDBC URL; example `jdbc:postgresql://<host>:5432/<dbname>?sslmode=require` |
| `DB_USERNAME`, `DB_PASSWORD` | Required application identity: `apaar_app` and its password |
| `FLYWAY_USER`, `FLYWAY_PASSWORD` | Required migration identity: `apaar_migrator` and its password |
| `DB_POOL_MAX` | Maximum Hikari connections, default 10 |
| `DB_POOL_MIN_IDLE` | Minimum idle connections, default 2; must not exceed pool maximum |
| `DB_CONNECTION_TIMEOUT_MS` | Pool acquisition timeout, default 30000 |
| `SEED_DEMO_DATA` | H2 defaults true; elsewhere defaults false; profile gate also applies |
| `JWT_SECRET` | Required in postgres; Base64 random signing key, recommend at least 48 random bytes; validated at startup |
| `JWT_EXPIRY_MINUTES` | JWT lifetime; postgres default 60, H2 default 1440 |
| `VERIFY_TOKEN_TTL_MINUTES` | Default opaque verification token lifetime; postgres 15, H2 4320; explicit API expiry hours still override it |
| `VERIFY_TOKEN_SECRET` | Reserved and **unused**: current tokens are random opaque database tokens, not signed tokens |
| `CORS_ALLOWED_ORIGINS` | Comma-separated exact browser origins; default `http://localhost:5173` |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Compose provisioning only; database defaults `apaar_connect`, user `apaar_owner`; password required |

Hikari retires connections after 25 minutes and idles surplus connections after
10 minutes. RDS/proxy/network idle timeouts vary: configure them above 25 minutes,
or adjust the checked-in pool settings for the actual deployment. Account for
**all backend replicas** when budgeting database connections. Flyway needs an
additional migration connection. Do not equate acquisition timeout with query timeout.

## Windows commands (PowerShell)

Run from the repository root; all paths are quoted. If using the portable tools
installed in this checkout, initialize the shell first:

```powershell
Set-Location -LiteralPath "C:\Users\Om Ukirde\hack hive\APAAR-ID---AuthenIQ"
$env:JAVA_HOME = (Get-ChildItem -LiteralPath ".\tools\java" -Directory | Select-Object -First 1).FullName
$env:PATH = "$env:JAVA_HOME\bin;$((Resolve-Path -LiteralPath '.\tools\maven\bin').Path);$env:PATH"
mvn -f "backend/pom.xml" test
```

Java 17 and Maven 3.9.9 can also be installed system-wide. Portable `tools/` files
are ignored and are not included in the branch.

If Java reports `Unable to establish loopback connection` from `PipeImpl` on
Windows, this machine's shortened temp path is incompatible with Java's native
socket handling. The verified workaround is session-local: use a normal absolute
temp directory before starting Maven/Java (IPv4 flags alone did not fix it):

```powershell
New-Item -ItemType Directory -Force -Path ".\tools\tmp" | Out-Null
$env:TEMP = (Resolve-Path -LiteralPath ".\tools\tmp").Path
$env:TMP = $env:TEMP
```

No server protocol, application code, or machine-wide network setting needs to change.

### H2 demo

In a fresh shell with no PostgreSQL variables:

```powershell
$env:SPRING_PROFILES_ACTIVE = "h2"
$env:SEED_DEMO_DATA = "true"
mvn -f "backend/pom.xml" spring-boot:run
```

No credentials are required. Demo account credentials remain described in the
original README and must never be used for production identities.

### Load your private environment file

This loader accepts the simple `KEY=value` format of the example, preserving
characters after the first equals sign and never printing values:

```powershell
Get-Content -LiteralPath ".\backend\.env" | ForEach-Object {
    if ($_ -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
```

Use raw values without wrapping quotes in that file. For local Compose set
`SPRING_PROFILES_ACTIVE=postgres,dev` and
`DB_URL=jdbc:postgresql://localhost:5432/apaar_connect` in your private copy.
Cloud connections keep `postgres` and `sslmode=require`.

### Local PostgreSQL 16 and role provisioning

```powershell
docker compose --env-file "backend/.env" up -d --wait
```

Compose binds port 5432 to loopback only, uses a named persistent volume, and
checks health using the configured database/user. Its plain local database uses
the explicit `dev` TLS exemption. Changing passwords in the env file does not
change credentials in an already initialized volume; rotate those with SQL.

Use `psql` (installed separately) as the provisioning administrator against the
**target application database**, first before migrating and again afterward:

```powershell
# For hosted PostgreSQL; omit for local non-TLS Compose.
$env:PGSSLMODE = "require"
psql --host "<host>" --port "5432" --username "<provisioner>" --dbname "<dbname>" --password --set "ON_ERROR_STOP=1" --file ".\db\init\create_app_user.sql"
psql --host "<host>" --port "5432" --username "<provisioner>" --dbname "<dbname>" --password
```

Inside the interactive psql session, set passwords using its hidden prompts:

```text
\password apaar_migrator
\password apaar_app
\q
```

Save the passwords privately in the matching environment variables. The script
creates LOGIN roles without embedded passwords, removes public schema CREATE,
and grants the provisioner membership in the migration role so it can grant
rights as object owner on managed PostgreSQL. It grants no membership to the app.
Use fresh dedicated roles; existing role memberships, database ownership, and
PUBLIC grants require an administrator audit. The application must never own
tables or inherit administrative/migration roles.

### Apply migrations, grant application access, then start

After loading the environment:

```powershell
mvn -f "backend/pom.xml" flyway:info
mvn -f "backend/pom.xml" flyway:migrate
mvn -f "backend/pom.xml" flyway:validate
# Repeat the administrator psql --file command above to grant access to created tables.
mvn -f "backend/pom.xml" spring-boot:run
```

The Maven plugin reads `DB_URL`, `FLYWAY_USER`, and `FLYWAY_PASSWORD` directly.
Use the same required TLS URL for these commands on hosted databases. Startup
also runs Flyway with the separate migration identity and validates the schema
using `apaar_app`; the runtime pool never uses migration credentials.
Use one controlled migration job before rolling out backend replicas.

Re-run `db/init/create_app_user.sql` after migrations creating additional tables
or sequences, extending its explicit allowlist when needed. It deliberately
does not grant the application access to `flyway_schema_history` or unrelated
provider tables. Ordinary tables receive SELECT/INSERT/UPDATE/DELETE; identity
sequences receive USAGE. Audit/history tables receive SELECT/INSERT only, with
UPDATE/DELETE/TRUNCATE explicitly revoked. No default grants expose future
provider or Flyway tables. Append-only restrictions protect against the app
identity; database owners and administrators remain trusted.

The existing README Docker shortcut predates this role/environment setup; use
this procedure for PostgreSQL.

## Supabase or Neon demo

The approved Supabase demo runs PostgreSQL **17.11**. AWS RDS and the default
Testcontainers target remain PostgreSQL **16**. V1/V2 use SQL available on both
versions; applied migrations remain unchanged. Flyway is pinned to 10.20.1 so
its PostgreSQL support covers version 17 as well as 16.

When using a Supabase session pooler with custom database roles, use its
`<role>.<project-reference>` login format for `DB_USERNAME` and `FLYWAY_USER`;
the server-side roles remain `apaar_app` and `apaar_migrator`. Both identities
have been verified against the approved demo. The application uses JDBC, not
Supabase's HTTP API; no publishable key, API secret key, or JWKS URL is required.

1. Create a fresh PostgreSQL project/database, preferably PostgreSQL 16 where
   offered. Copy its **direct database** host, port and database name from the
   provider dashboard. For Supabase, direct endpoints may require IPv6; use a
   supported session endpoint if direct connectivity is unavailable. Avoid
   transaction pooling for migrations and schema management.
2. Verify that the provisioning identity can create/manage separate roles and
   grant schema rights. Provider restrictions vary. If those rights are absent,
   ask the provider/project administrator to provision the roles; do not silently
   run the application with the project owner identity.
3. Set the JDBC `DB_URL` with `sslmode=require`, provision roles, set passwords,
   migrate with `apaar_migrator`, run the grants script again, then start with
   `apaar_app`. Never use a Supabase HTTP API URL or API key as a JDBC credential.
4. For a hosted demo with synthetic accounts, explicitly use `postgres,dev`
   and `SEED_DEMO_DATA=true`, retaining TLS in the URL. Use a separate disposable
   database from production. `postgres` alone never seeds.

Provider references: [Supabase connections](https://supabase.com/docs/guides/database/connecting-to-postgres)
and [Neon connection settings](https://neon.com/docs/connect/connect-from-any-app).

## AWS RDS production: ap-south-1 (Mumbai)

- Provision RDS PostgreSQL 16 in `ap-south-1` in private subnets across at least
  two Availability Zones, enable Multi-AZ, and disable public accessibility.
- Allow inbound PostgreSQL only from the backend security group. The database
  must be reachable only from backend infrastructure, including its controlled
  migration job. Never connect to RDS directly from the browser/frontend.
- Enable KMS encryption at creation, encrypted snapshots, automated backups
  with a retention period agreed for the service, and point-in-time recovery.
  Test restore procedures and monitor storage, connections and failovers.
- Keep app, migration and provisioner passwords separate in Secrets Manager.
  Inject the app and migration values as environment variables at deployment;
  restrict access and rotate credentials with coordinated pool restarts.
- Use `sslmode=require` for the enforced minimum TLS setting and enforce TLS
  server-side. This mode encrypts transport but does not verify server identity.
  Certificate-verifying TLS requires a separately reviewed configuration change
  to the current exact-mode guard; do not append conflicting sslmode parameters.
- Run the same provisioning/migration/grant sequence from inside the backend
  network, back up before upgrades, deploy with `postgres` and
  `SEED_DEMO_DATA=false`, and run the JVM in UTC. Keep migration credentials out
  of frontend artifacts and browser configuration.

References: [RDS VPC/subnets](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_VPC.WorkingWithRDSInstanceinaVPC.html),
[RDS security](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.html).
Cloud provisioning is manual; this change does not create cloud resources.

## Validation and remaining release check

Run `mvn -f "backend/pom.xml" test`. The original 12 unit tests remain present.
Only the existing `ValidationEngineTest.testDuplicateSameInstitution` fixture
and the repository stub it uses changed: the matching student now belongs to
another programme at the same institution. Expected rule/status are unchanged.
Additional H2 tests cover reporting year boundaries, counted-status lookups,
TLS enforcement, seeder exclusion, and complete demo startup/seeding.

`PostgresDatabaseTest` uses PostgreSQL 16 Testcontainers with
`@Testcontainers(disabledWithoutDocker = true)`. It provisions roles using the
actual manual script, migrates a clean DB as `apaar_migrator`, starts Spring Boot
with Hibernate `validate` and the app identity, and verifies partial uniqueness,
cross-institution reporting, denied app DDL, and append-only log/history grants.
Docker is unavailable on the development machine, so its four tests skip.
**Run them in Docker-enabled CI before a production deployment.**

### Opt-in live database checks

`ExternalPostgresSmokeTest` is disabled unless `RUN_EXTERNAL_DB_TESTS=true`.
After loading your private environment file as described above:

```powershell
$env:RUN_EXTERNAL_DB_TESTS = "true"
mvn -f "backend/pom.xml" "-Dtest=External*Test" test
Remove-Item -LiteralPath "Env:\RUN_EXTERNAL_DB_TESTS"
```

Run this only against an approved demo/test database. The external suites validate all 10 tables
and their 10 foreign keys, Hibernate startup, Flyway checksums, app-role CRUD,
partial uniqueness, reporting year boundaries, append-only permissions, and
denied table creation. Test records are transactional and rolled back, including
audit inserts; identity sequence counters may advance. Startup still runs any
pending Flyway migrations, so review them first. The test requires no secrets
in source code and is skipped by the ordinary local test command.

`ExternalSchemaContractTest` compares every mapped column's type, length,
nullability and identity strategy, every relationship, unique mapping, V2 index,
and V1 CHECK name with the live catalog. It verifies migration history and role
privileges, and tests orphan rejection and migration-role DDL within rollbacks.
`ExternalBackendApiTest` exercises JWT login, student services/controllers,
validation, audit/history generation, institution/programme reads, errors and CORS.
`ExternalHttpStartupTest` starts real Tomcat on an ephemeral port and makes HTTP
requests through Spring Security to the actual backend. No server remains running
after the test JVM exits. No institution/programme write API or student DELETE API
exists; write/delete verification uses existing repositories rather than adding endpoints.

See [DATABASE_VERIFICATION.md](DATABASE_VERIFICATION.md) for the complete executed report.

The Supabase 17.11 deployment has V1 and V2 applied. It uses generated app-role,
migration-role and JWT credentials in the ignored local `backend/.env`, with
demo seeding disabled. No API keys or provisioning password are saved there.
Do not copy that file into a PR or container image. PostgreSQL 16 integration
execution remains pending a Docker-enabled environment; a successful live 17
check does not replace that release check.

To deploy, the operator still supplies the target host/database, provisioning
access, role passwords, JWT signing key, allowed frontend origin, and the AWS
network/security/backup/secret settings. Do not send those secrets in a PR or chat.
