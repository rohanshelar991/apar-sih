# Vercel + Render + a separate Supabase demo project

Status: code/configuration prepared; no cloud deployment has been performed.
Use a **new, separately isolated Supabase demo project**. The old production-labelled
project is blocked by the startup guard. Never copy the existing ignored local
environment into Render: it points at that retired target.

The user has now identified the new project as **`hxmlezblzvdbacebsmcb`**.
The guard is pinned to that exact reference, Mumbai Session Pooler host,
database `postgres`, and session port 5432. Both database login identities must
carry that project suffix. Other project references and transaction port 6543
are rejected before Spring creates Flyway or application connections.
**Connection/migration authorization is still pending. No new-project connection
or provisioning has been performed.** See [exact Render entry instructions](RENDER_ENVIRONMENT.md).

The hosted path is Vercel HTTPS → Render HTTPS → Supabase Session Pooler with SSL.
Spring Boot owns authentication through `public.user_accounts`, BCrypt and JWT.
Supabase Auth users, publishable keys, secret API keys, and a Supabase frontend
SDK are not needed. All three existing application roles and API paths remain.

## Release prerequisites

1. Review [security changes and remaining limits](SECURITY_DEPLOYMENT_REVIEW.md).
2. Create/identify the separate Supabase demo project and confirm it has no
   production consumers. Do not reuse the old project, its passwords or JWT key.
3. Supply its project reference, Session Pooler host/port, application-role and
   migration-role credentials through Render's environment settings only.
4. Have an authorized operator configure least-privilege roles using
   `db/init/create_app_user.sql`, with passwords delivered privately. That SQL
   is a manual role-setup script, not a migration. Do not run it against the old DB.
5. Review V1/V2 and back up any existing data in the new target before initial
   startup. Flyway creates missing schema versions under the migration identity,
   validates checksums and records each successful version. Hibernate only
   validates. Existing tables must not be adopted with automatic baselining or
   repaired by editing migrations. Unexpected schema/history: stop and investigate.
6. No accounts or datasets are seeded. Old-project accounts are not copied.
   Account creation in the new project requires a separate approved process.
   The three UI personas will not authenticate until their new-project accounts exist.

The existing H2 profile and PostgreSQL 16 Compose file are development/test
utilities only. They are not deployed, used as fallbacks, or dependencies of
Vercel/Render. Production startup fails on missing configuration or database errors.

## Render

Use the repository-root `render.yaml`, which defines a Docker web service only,
not a Render database. Connect the GitHub repository in Render and select the
intended reviewed branch. Do not apply the Blueprint/create the service before
the new database and secrets are ready: initial service creation can start a build.

Set these in Render. Examples contain placeholders only:

| Variable | Value / requirement |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `postgres,render` |
| `SUPABASE_PROJECT_REF` | Reference of the **new** separate demo project |
| `DB_URL` | `jdbc:postgresql://<new-session-pooler-host>:5432/postgres?sslmode=require&connectTimeout=15&socketTimeout=30` |
| `DB_USERNAME` | `apaar_app.<new-project-ref>` or the configured application role with that project suffix |
| `DB_PASSWORD` | New project's application-role password |
| `FLYWAY_USER` | `apaar_migrator.<new-project-ref>` |
| `FLYWAY_PASSWORD` | New project's migration-role password |
| `JWT_SECRET` | Fresh Base64 encoding of at least 48 cryptographically random bytes |
| `JWT_EXPIRY_MINUTES` | `60` |
| `VERIFY_TOKEN_TTL_MINUTES` | `15` |
| `VERIFY_TOKEN_SECRET` | Reserved; current verification tokens are opaque database tokens, not signed tokens. This variable is not consumed by existing code. |
| `CORS_ALLOWED_ORIGINS` | Exact assigned frontend origin, e.g. `https://<frontend-project>.vercel.app`; no trailing slash or wildcard |
| `DB_POOL_MAX` / `DB_POOL_MIN_IDLE` | `10` / `2`; stay within the pooler's connection allowance |
| `DB_CONNECTION_TIMEOUT_MS` | `30000` |
| `SEED_DEMO_DATA` | `false` |
| `PORT` | Provided by Render; application reads it, default 10000 in render profile |

The guard checks both database identities against the new project reference,
requires the Supabase Session Pooler on 5432 and `sslmode=require`, rejects the
old project, and forbids `h2`/`dev` in the Render profile. A `render` profile
without `postgres` fails. CORS requires exact public HTTPS origins and the JWT
key must decode to at least 48 bytes. Entropy still depends on proper generation.

Render build/start are defined by `backend/Dockerfile`:

- Build stage: Maven 3.9.9 / Temurin Java 17; `mvn -B -ntp -DskipTests package`
  inside the backend directory. Tests run in CI before deployment.
- Start: `java -jar /app/app.jar`, as a non-root user, UTC JVM, binding `0.0.0.0`
  on `${PORT}`. No computer/IDE/environment file is needed in the container.
- Health check: `/api/health`; 200 `{"status":"UP"}` for a valid DB connection,
  otherwise 503 `{"status":"DOWN"}`. It exposes no database details.

To build the same image manually from the repository root, with Docker available:

```sh
docker build -f "backend/Dockerfile" -t apaar-garuda-netra-api "backend"
```

Do not put real secrets into Docker build arguments or image layers. `.env` files
are excluded from the Docker context. Render runs Flyway at application startup;
do not run a second migration step in CI. Its schema-history locking coordinates
concurrent startups. Never enable Flyway clean, automatic baseline or Hibernate DDL.

## Vercel

Create a Vercel project linked to the same GitHub repository:

- Root Directory: `frontend`.
- Framework: Vite; Node 22.
- Install: `npm ci`; Build: `npm run build`; Output: `dist`.
- Set exactly one application frontend variable:
  `VITE_API_BASE_URL=https://<actual-render-service>.onrender.com/api`.
- Set the actual assigned Vercel origin in Render `CORS_ALLOWED_ORIGINS`.
- If a custom frontend domain is added, explicitly add its HTTPS origin in CORS.
  Preview domains are not automatically trusted. Use a separately controlled
  demo environment for previews rather than a wildcard origin.

`frontend/vercel.json` preserves SPA refresh/deep links and public static assets,
adds response security headers, and disables built-in Git autodeployment so it
cannot bypass CI. The API calls the HTTPS Render service directly, not a localhost
proxy. Vite rejects production builds without a configured HTTPS API URL ending
in `/api`. `frontend/.env.example` has only a public placeholder; never put DB
passwords, Supabase secret keys, JWT keys or migration credentials into Vercel.

QR links continue to use the frontend's actual `window.location.origin`, so
generated links resolve to the deployed Vercel/custom domain. API response shapes
and paths are unchanged. The API timeout is 120 seconds for cold starts, with a
clear login-network message. There is **no automatic retry of writes/uploads**.
A browser timeout does not prove the server rolled back; inspect audit/batch state
before manually resubmitting a write. Consider an always-on Render service for demos.

## GitHub Actions and deployment gates

`.github/workflows/ci-deploy.yml` runs on pushes and pull requests:

1. Java 17 backend `mvn -B -ntp -f backend/pom.xml verify`.
2. Frontend `npm ci` and production build. Its `.invalid` API URL is compile-only;
   that build is not deployed. Docker-enabled CI can run PostgreSQL 16 Testcontainers.
3. Deployment only on `main`, after both jobs pass, with repository variable
   `DEPLOY_ENABLED=true` and the protected GitHub environment `production`.

Keep `DEPLOY_ENABLED` absent/false until the new target, security review and
deployment permissions are ready. Configure required reviewers for the production
environment and required backend/frontend checks on the main branch.

Configure in the GitHub **production environment**, not workflow source:

| Kind | Names |
| --- | --- |
| Secrets | `RENDER_API_KEY`, `VERCEL_TOKEN` |
| Variables | `RENDER_SERVICE_ID`, `RENDER_BACKEND_URL`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID` |
| Repository deployment gate | `DEPLOY_ENABLED` |

For Render, retain Auto-Deploy **Off** as in `render.yaml`. The workflow requests
deployment of the exact checked Git SHA through the Render API, waits for `live`,
checks that SHA and `/api/health`, then deploys Vercel. It does not use a hook that
might deploy an unchecked later commit. It never prints API response bodies or keys.

For Vercel, obtain the project/team IDs and authorize a deployment token privately.
The CLI runs from the repository root, using the project's configured Root Directory
`frontend`. The workflow pins CLI 65.9.0 and uses:

```sh
vercel pull --yes --environment=production --token="$VERCEL_TOKEN"
vercel build --prod --token="$VERCEL_TOKEN"
vercel deploy --prebuilt --prod --token="$VERCEL_TOKEN"
```

`vercel pull` downloads Vercel-managed build configuration; `.vercel/` is ignored.
The deploy build uses the actual Vercel API variable, not the CI placeholder.
Deployment across two providers is not atomic. If Vercel fails after Render goes
live, report that partial state and roll back application deployments as needed;
do not delete or rewrite database records to make a deployment appear successful.

## Live verification after authorized deployment

Use [the verification checklist](DEPLOYMENT_VERIFICATION.md). First verify health,
new-project identity, migration history and Hibernate validation. Then test the
three **new-project** accounts through real Spring Boot login, JWT expiry/profile
and allowed/denied roles. These checks are not substitutes for real login.
Uploads, corrections, QR issuance/verification and successful logins write records
or audit history. Obtain explicit disposable-fixture authorization first and
preserve all existing records. Verify persistence across a Render restart without
deleting data. No deployed URLs or successful cloud logins are claimed yet.

Official references used to prepare this configuration:

- [Vercel Vite SPA configuration](https://vercel.com/docs/frameworks/frontend/vite)
- [Vercel GitHub Actions build/deploy](https://vercel.com/kb/guide/how-can-i-use-github-actions-with-vercel)
- [Render Blueprint reference](https://render.com/docs/blueprint-spec)
- [Render deploy API, including commitId](https://api-docs.render.com/reference/create-deploy)
