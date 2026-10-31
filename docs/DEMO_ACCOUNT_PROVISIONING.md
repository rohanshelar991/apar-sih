# Proposed Supabase demo-account provisioning — awaiting approval

**Status: NOT EXECUTED. No account or institution has been inserted.**

The running application uses `postgres`, the existing Supabase PostgreSQL 17.11
Session Pooler with TLS, Flyway V1/V2, and Hibernate `validate`. All 10 application
tables currently contain zero rows. The three persona buttons exist, but their
PostgreSQL accounts do not. Authentication success must not be claimed yet.

## Exact proposed records

| Persona | Email | Existing application role | Institution association |
| --- | --- | --- | --- |
| Institution Administrator | `admin@bmsce.ac.in` | `ROLE_INSTITUTION_ADMIN` | Foreign key to `INST001` |
| AICTE National Regulator | `regulator@aicte-india.org` | `ROLE_AICTE_REGULATOR` | `NULL`, matching the existing account design |
| Authorized Verifier / Employer | `verifier@tcs.com` | `ROLE_AUTHORIZED_VERIFIER` | `NULL`, matching the existing account design |

Full names will match the existing H2 demo: `Dr. S. K. Ramesh (Principal / Admin)`,
`Dr. Amit Banerjee (AICTE Regional Advisor)`, and
`Rajesh Kulkarni (Lead Talent Verification, TCS)`, respectively.

Because Supabase has no institution rows, the administrator requires **one new
institution**: code `INST001`, name `BMS College of Engineering`, state
`Karnataka`, city `Bengaluru`, status `ACTIVE`. These are the existing demo
institution values. Its database-generated ID will be used; no ID is assumed.

Initial approved provisioning would therefore insert **four rows total**:
one institution and three user accounts. No programmes, students, batches,
validation issues, histories, verification requests, or tokens will be seeded.

## Required user-account fields

| Column | Proposed value / handling |
| --- | --- |
| `id` | Database-generated identity; never assigned or reset |
| `email` | Lowercase email above; required, unique, at most 120 characters |
| `password_hash` | Salted BCrypt hash only; required, stored in VARCHAR(128) |
| `full_name` | Existing demo name above; required, at most 120 characters |
| `role` | Exact existing role above; required, at most 32 characters |
| `institution_id` | Resolved `INST001` ID for administrator; NULL for the other two |
| `active` | `true` for all three |
| `created_at` | Creation time in UTC, mapped as TIMESTAMP WITHOUT TIME ZONE |

## Password handling

Use the application's existing Spring Security `BCryptPasswordEncoder`, the
same encoder used by `AuthService` and the H2 demo seeder. Its current default
strength is 10, with a fresh random salt for each hash. Verify each generated
hash with `PasswordEncoder.matches` before insertion.

To preserve the existing one-click persona buttons without changing login
logic or API contracts, the proposal uses their **existing public demo
passwords**. Their values are already supplied by the current demo-accounts API;
they will not be repeated in logs, SQL files, this plan, or new configuration.
Only BCrypt hashes go into PostgreSQL. These are public demo identities, suitable
only for this isolated synthetic-data demo, never for production or real data.

Using new private passwords instead would require a separately agreed change to
the one-click demo experience; exposing private passwords through the public
demo-accounts endpoint is not proposed.

## Separate manual process, only after approval

1. Obtain explicit approval for these exact three demo accounts, the new
   `INST001` institution, and the subsequent normal login tests.
2. Load credentials from ignored `backend/.env` into process memory. Confirm the
   target is this Supabase demo Session Pooler, PostgreSQL 17.11, TLS active,
   Flyway V1/V2 valid, and Hibernate validation successful. Never echo secrets.
3. Use the existing least-privilege application database role for DML. Do not
   grant privileges, change roles, run migrations, or use a schema-owner role.
4. Use a standalone provisioning command, never application startup, the broad
   demo seeder, or a Flyway migration. Its default mode must be a read-only plan;
   applying requires an explicit approval argument after approval here.
5. Preflight all records and associations before writes. Confirm the database
   still contains only the expected isolated demo data. If emails or `INST001`
   already exist, validate their names, roles, active flags, associations, and
   BCrypt password matches. Reuse exact matches; abort on differences. Never
   reset an existing password, reactivate an account, or overwrite a record.
6. In one transaction, take a provisioning advisory lock, recheck the preflight,
   insert only missing approved records with prepared statements, resolve the
   generated institution ID, and verify all three account mappings. Commit only
   when every check succeeds; otherwise roll back. No UPDATE, DELETE, TRUNCATE,
   DDL, sequence reset, or conflict-update statements are part of this process.
7. Report created/reused record counts and roles, with no passwords, hashes,
   tokens, environment values, or database connection strings in output.

This document prepares the process; the apply command has not been implemented
or run. Implementation and execution occur only after approval. Re-running the
approved process must be idempotent and must never overwrite existing records.

## Post-approval verification on the running Supabase backend

- Use the existing `/api/auth/login` with each approved persona's credentials.
  Require a real JWT and correct response role; verify `/api/auth/me` through
  that JWT. Do not substitute locally manufactured tokens for login tests.
- Confirm the institution administrator is associated with `INST001`; regulator
  and verifier have the expected absent institution association.
- Check the existing authorization matrix using read-only endpoints: institution
  and regulator can access permitted student/institution routes; only regulator
  can access audit/regulator routes; verifier is denied those protected routes.
- Require 401 for missing/invalid JWTs and incorrect passwords, and 403 for
  authenticated role violations. Preserve all authentication/authorization code.
- Confirm the frontend's three buttons route to the appropriate pages. No H2
  fallback, schema recreation, or broad synthetic dataset is allowed.
- Successful login tests normally append **three LOGIN_SUCCESS audit records**
  through existing `AuthService`/`AuditService`. These normal inserts are included
  in the approval request. Existing audit/history records will never be deleted.
- Report actual per-persona login, JWT/profile, role-authorization, and frontend
  results, including failures. Do not call the login setup complete before they pass.

**Approval requested:** create the one institution and three demo accounts
above, then test all three logins against Supabase with normal audit logging.
