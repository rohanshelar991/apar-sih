# Reviewed new-project role bootstrap — awaiting approval

Target: **hxmlezblzvdbacebsmcb only**. No SQL, connection, migration, role creation
or account provisioning has been performed. Passwords were prepared offline.
Do not use the older generic `create_app_user.sql` as a substitute for these
reviewed, gated new-project scripts.

## Migration and privilege review

V1 creates ten public tables, ten BIGINT identity sequences, primary/foreign keys
and checks. V2 creates indexes and adds unique constraints, including the partial
student uniqueness index. Flyway also owns its schema-history table. V1/V2 are
unchanged. The migration role must **own** the tables it creates: granting DML on
administrator-owned tables would not permit V2's ALTER TABLE / CREATE INDEX work.

| Capability | apaar_app | apaar_migrator |
| --- | --- | --- |
| LOGIN / CONNECT to postgres | Yes | Yes |
| public schema USAGE | Yes | Yes |
| public schema CREATE | No | Yes |
| Own new application tables, indexes and identity sequences | No | Yes, by creating them through Flyway |
| Eight ordinary application tables | SELECT / INSERT / UPDATE / DELETE | Owner privileges |
| audit_logs / student_history | SELECT / INSERT only | Owner privileges |
| Application identity sequences | USAGE only; no sequence reset privilege | Owner privileges |
| flyway_schema_history | No access | Owner privileges |
| Superuser / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS | No | No |
| Inherited privileged roles | No | No |

The eight ordinary tables are institutions, programmes, students, upload_batches,
validation_issues, verification_tokens, user_accounts and verification_requests.
No TRUNCATE, REFERENCES, TRIGGER, ownership or grant-option privileges are granted
to the application. The migrator role is deliberately DDL-capable; keep its
credentials separate and never use it as the runtime datasource identity.

Persistent public-schema migrations are prevented for apaar_app. PostgreSQL may
still grant session-local TEMP privileges through PUBLIC; these are not persistent
schema migration privileges. This procedure does not globally revoke database TEMP
from Supabase's platform roles. It does revoke **public-schema CREATE from PUBLIC**
so that the application cannot inherit CREATE implicitly. That schema ACL change
must be reviewed for the isolated new project; no existing table/data is altered.

## Password preparation and private delivery

The offline generator creates two independent 256-bit random passwords, with
separate salts and PostgreSQL SCRAM-SHA-256 verifiers (16,384 PBKDF2 iterations).
This is PostgreSQL role authentication, **not** BCrypt application-account hashing.
The existing Spring Boot user-account BCrypt encoder is unchanged.

Private directory on this machine:

`C:\Users\Om Ukirde\.codex\private\garuda-db-bootstrap-hxmlezblzvdbacebsmcb`

- `role-passwords.private.json`: the two plaintext passwords, for private transfer
  to the appropriate Render password fields only.
- `01-bootstrap.private.sql`: role-creation SQL with SCRAM verifiers substituted;
  it contains no plaintext passwords. Verifiers are still sensitive: keep this
  file private and do not commit or share it.

The directory has inheritance disabled and a current-user-only Windows ACL.
No passwords or verifiers were printed, put into command arguments or committed.
The generator refuses to overwrite an existing credential directory. Its
verification checks both SCRAM keys against each generated password offline.
No regeneration or rotation happens automatically.

## Manual execution — only AFTER explicit approval

### A. Create the two roles and schema permissions

1. Confirm the administrator password has been rotated privately. Do not paste it
   into chat. Open the new project at:
   `https://supabase.com/dashboard/project/hxmlezblzvdbacebsmcb/sql/new`.
2. Verify the project reference in the dashboard address bar and project settings.
   Use SQL Editor as `postgres`, database `postgres`. Do not open the old project.
   A normal PostgreSQL connection reports database/role, not a universally
   available Supabase project identifier. The SQL confirmation string is an
   **operator acknowledgement, not server-side proof of project identity**.
3. Review the committed `db/init/supabase_demo_bootstrap.sql` template, then open
   the private `01-bootstrap.private.sql` locally. It has the real private SCRAM
   verifiers already substituted. Do not open/share the password JSON in chat.
4. Only after approval, replace `__CONFIRM_NEW_PROJECT_REF__` in that private SQL
   with `hxmlezblzvdbacebsmcb`. The unmodified file intentionally cannot create roles.
5. Paste the complete private SQL into a new editor query and run the **whole
   transaction**. It aborts if either role already exists or if any application
   table/history table already exists. It never rotates an existing password,
   changes ownership, drops a table, or adopts an existing schema.
6. Check the non-secret role-attribute result. Both roles should have LOGIN;
   superuser, createdb, createrole, inherit, replication and bypassrls should be false.
   No application tables or records should have been created by this stage.

The script grants postgres membership in apaar_migrator solely so the
administrator can SET ROLE for stage B. It never grants that membership to
apaar_app or Supabase frontend API roles.

### B. Run migrations — separate authorization required

Stop after A. Role-bootstrap approval does not authorize migrations or account
creation. After separate migration authorization, verify new-project routed
identities and TLS read-only, then run Flyway **as apaar_migrator**, never as
postgres or apaar_app. The application remains stopped until grants are ready;
do not trigger full application startup and assume the runtime role already has
table privileges. Use an explicitly reviewed migration-only operation.

Flyway creates/validates V1/V2 and its history. If unexpected existing objects,
owners, failures or history appear, stop. Do not use clean, automatic baseline,
repair, DROP, owner changes or edited applied migrations as a shortcut.

### C. Grant runtime access after successful migrations

1. In the same verified new project's SQL Editor, open
   `db/init/supabase_demo_post_migration_grants.sql` as postgres.
2. After authorization, replace its project acknowledgement placeholder with
   `hxmlezblzvdbacebsmcb` and execute the complete transaction.
3. It requires all ten tables and Flyway history to be migrator-owned, validates
   the role attributes/memberships and successful V1/V2 history, grants only the
   named tables/sequences, and checks effective privileges before commit.
4. Unexpected PUBLIC/inherited rights cause an error and rollback rather than
   a broad automatic revocation from unrelated Supabase roles.

Default grants do not expose every future table or Flyway history. A future
migration adding a runtime table requires a separate named-table grant review.

## Transactions, conflicts and rollback

Each SQL file uses its own BEGIN/COMMIT, advisory transaction lock, lock timeout
and statement timeout. Any exception prevents that stage from committing. If
SQL Editor leaves a transaction open/aborted, issue `ROLLBACK;` before further
work. Do not rerun role creation to reset passwords. Stage C failure rolls back
its grants; it does not undo separately committed roles or migrations.

Existing tables/data are preserved by refusing conflicting bootstrap objects,
never reassigning owners, and performing no table DML. No demo institution,
application account, Supabase Auth user or other dataset is created. If roles or
application objects already exist, stop for a new read-only review rather than
executing the fresh-bootstrap procedure.

## Session Pooler identities and Render fields

Use the provided session host on **5432**, not transaction port 6543:

`jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require`

| Purpose | Database role | Session Pooler username |
| --- | --- | --- |
| Runtime datasource | apaar_app | `apaar_app.hxmlezblzvdbacebsmcb` |
| Flyway | apaar_migrator | `apaar_migrator.hxmlezblzvdbacebsmcb` |
| SQL Editor/bootstrap administrator | postgres | `postgres.hxmlezblzvdbacebsmcb` if connecting through the pooler |

Role creation uses the bare names; the `.project-ref` suffix is the pooler routing
identity, not part of the PostgreSQL role name. Enter each role's plaintext
password from the private JSON only in its matching Render environment password
field, never as the other role's password. Saving configuration does not authorize
deployment or connection; choose Save only and leave deployment disabled.

Official references:
- [Supabase custom-role pooler username format](https://supabase.com/kb/troubleshooting/tenant-or-user-not-found)
- [Supabase session connections](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [PostgreSQL accepts SCRAM verifiers in CREATE ROLE](https://www.postgresql.org/docs/16/sql-createrole.html)

## Actual verification status

- COMPLETED: V1/V2 privilege review, SQL templates and offline credential preparation.
- COMPLETED: independent-password and SCRAM derivation checks; private SQL contains
  no plaintext passwords and retains the closed execution gate.
- NOT TESTED: SQL execution, Supabase role existence, effective live privileges,
  pooler login, migrations and application startup. No database was contacted.
- WAITING FOR APPROVAL: stage A only; migrations, stage C and account provisioning
  remain separately gated.
