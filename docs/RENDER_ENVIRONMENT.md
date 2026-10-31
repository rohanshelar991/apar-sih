# New Supabase target: Render environment entry instructions

**Configuration prepared only. No connection, migration, role creation, account
creation, or data insertion is authorized or performed by this change.**

Approved project reference: `hxmlezblzvdbacebsmcb`. The application remains
React/Vite + Spring Boot + JPA/JDBC + Flyway + Spring Security JWT. Do not install
Supabase JavaScript/SSR packages or Prisma. Do not use `NEXT_PUBLIC_*`,
`DATABASE_URL`, `DIRECT_URL`, Supabase Auth, or frontend Supabase credentials.
Those copied template names are not the variables consumed by this backend.

## Exactly where to enter values

In Render Dashboard, select the **Spring Boot backend Web Service** →
**Environment** → **Environment Variables** → **Add Environment Variable**.
Use the key/value entries below. Do not enter these in Vercel, GitHub source,
Docker build arguments, screenshots, terminal command arguments, or this document.

If a Render service already exists, choose **Save only** from the save dropdown.
Do not choose Save and deploy or Save, rebuild, and deploy. Do not trigger a
manual deploy, enable automatic deployment, or apply the Blueprint yet.
If no service exists, prepare values privately and wait: creating a service or
applying a Blueprint can start its first deployment and connect to the database.

## Render values

```dotenv
SPRING_PROFILES_ACTIVE=postgres,render
SUPABASE_PROJECT_REF=hxmlezblzvdbacebsmcb
DB_URL=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require&connectTimeout=15&socketTimeout=30
DB_USERNAME=apaar_app.hxmlezblzvdbacebsmcb
DB_PASSWORD=<new-project-application-role-password>
FLYWAY_USER=apaar_migrator.hxmlezblzvdbacebsmcb
FLYWAY_PASSWORD=<new-project-migration-role-password>
JWT_SECRET=<fresh-base64-encoding-of-at-least-48-random-bytes>
JWT_EXPIRY_MINUTES=60
VERIFY_TOKEN_TTL_MINUTES=15
CORS_ALLOWED_ORIGINS=https://<actual-vercel-project>.vercel.app
DB_POOL_MAX=10
DB_POOL_MIN_IDLE=2
DB_CONNECTION_TIMEOUT_MS=30000
SEED_DEMO_DATA=false
```

Render provides `PORT`; the application binds `0.0.0.0`. `VERIFY_TOKEN_SECRET`
is reserved in the example but is not used by the existing opaque-token design.
Do not mistake it for the JWT signing secret or claim that it signs verification
tokens. Never reuse old-project secrets.

The supplied `postgres.hxmlezblzvdbacebsmcb` Session Pooler identity is the
database administrator/bootstrap identity. The deployment examples use separate
least-privilege application and migration roles instead. **Those roles and their
passwords have not been verified or created in the new project.** Configuring
their names is not evidence that the roles exist. Creating them requires a
separate authorized step; no such SQL was run here. Do not use the administrator
password as if it were an application-role password.

The real database password supplied in chat was not written to these examples,
source, logs, or the old ignored local environment. Rotate that exposed password
privately before deployment. Do not send the replacement in chat; enter it only
in the appropriate private credential setup/Render field after authorization.

## Why Session Pooler 5432

Use the provided shared **session-mode** pooler at port **5432** for this JDBC
application and Flyway. Do not use transaction port 6543 or the Prisma-specific
`pgbouncer=true` template. Do not substitute the direct `db.<reference>.supabase.co`
connection in this deployment; the guard deliberately requires the approved
pooler host/port/database and both routed usernames.

Before Spring creates Flyway or the application datasource, the guard requires:

- Exactly `SUPABASE_PROJECT_REF=hxmlezblzvdbacebsmcb`.
- Host `aws-0-ap-south-1.pooler.supabase.com`, port 5432, database `postgres`.
- Both `DB_USERNAME` and `FLYWAY_USER` ending in `.hxmlezblzvdbacebsmcb`.
- `sslmode=require`, with no TLS-disabling override or credentials in the URL.
- PostgreSQL/Render profiles, Flyway enabled, Hibernate `ddl-auto=validate`.

The old production-labelled project remains blocked. This is a pre-connection
configuration check, not a claim that the new database has been contacted or its
schema validated. After explicit connection authorization, first perform a
read-only target/TLS/identity audit and review pending migrations. Migration
execution and any later account provisioning need their own authorized scope.
Do not use a bare Maven `flyway:migrate` command to bypass the Spring startup guard.

Keep `DEPLOY_ENABLED` unset/false in GitHub and Render Auto-Deploy off until
authorization. The Vercel frontend needs only
`VITE_API_BASE_URL=https://<actual-render-service>.onrender.com/api`.

Official dashboard instructions: [Render environment variables and Save only](https://render.com/docs/configure-environment-variables).
