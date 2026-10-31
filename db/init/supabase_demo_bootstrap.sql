-- REVIEW TEMPLATE ONLY. NOT A FLYWAY MIGRATION. Do not execute before approval.
-- SQL Editor URL MUST be /project/hxmlezblzvdbacebsmcb/sql/...
-- The confirmation below is an operator acknowledgement, NOT project detection.
-- No standard PostgreSQL query independently proves a Supabase project reference.
-- Use the offline generator for private SCRAM verifiers; do not paste passwords.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';

DO $bootstrap$
DECLARE
    confirmed_project text := '__CONFIRM_NEW_PROJECT_REF__';
    app_verifier text := '__APP_SCRAM_VERIFIER__';
    migrator_verifier text := '__MIGRATOR_SCRAM_VERIFIER__';
    object_name text;
BEGIN
    IF confirmed_project <> 'hxmlezblzvdbacebsmcb' THEN
        RAISE EXCEPTION 'STOP: verify the new project in the dashboard and explicitly confirm it only after approval.';
    END IF;
    IF current_database() <> 'postgres' OR current_user <> 'postgres' THEN
        RAISE EXCEPTION 'STOP: use the new project SQL Editor as postgres in database postgres.';
    END IF;
    PERFORM pg_advisory_xact_lock(262632027);
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname IN ('apaar_app','apaar_migrator')) THEN
        RAISE EXCEPTION 'STOP: a role already exists; no password reset or role overwrite is permitted.';
    END IF;
    FOREACH object_name IN ARRAY ARRAY['institutions','programmes','students','upload_batches',
        'validation_issues','student_history','verification_tokens','audit_logs',
        'user_accounts','verification_requests','flyway_schema_history']
    LOOP
        IF to_regclass(format('public.%I',object_name)) IS NOT NULL THEN
            RAISE EXCEPTION 'STOP: existing application object %. Review ownership and migration history separately.',object_name;
        END IF;
    END LOOP;
    IF app_verifier NOT LIKE 'SCRAM-SHA-256$16384:%$%:%'
        OR migrator_verifier NOT LIKE 'SCRAM-SHA-256$16384:%$%:%'
        OR app_verifier = migrator_verifier THEN
        RAISE EXCEPTION 'STOP: supply two distinct privately generated SCRAM verifiers.';
    END IF;
    EXECUTE format('CREATE ROLE apaar_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOREPLICATION NOBYPASSRLS PASSWORD %L',app_verifier);
    EXECUTE format('CREATE ROLE apaar_migrator LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOREPLICATION NOBYPASSRLS PASSWORD %L',migrator_verifier);
END
$bootstrap$;

-- This removes the implicit public-schema CREATE privilege, if present.
-- It affects the PUBLIC privilege group, not table data. Review before approval.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT CONNECT ON DATABASE postgres TO apaar_app, apaar_migrator;
GRANT USAGE ON SCHEMA public TO apaar_app;
REVOKE CREATE ON SCHEMA public FROM apaar_app;
GRANT USAGE, CREATE ON SCHEMA public TO apaar_migrator;

-- Administrator may SET ROLE for the post-migration grant stage. Never grant
-- migrator membership to apaar_app, anon, authenticated or authenticator.
GRANT apaar_migrator TO postgres;
ALTER ROLE apaar_app IN DATABASE postgres SET search_path = public;
ALTER ROLE apaar_migrator IN DATABASE postgres SET search_path = public;
COMMENT ON ROLE apaar_app IS 'APAAR GARUDA NETRA runtime; isolated demo hxmlezblzvdbacebsmcb';
COMMENT ON ROLE apaar_migrator IS 'APAAR GARUDA NETRA Flyway owner; isolated demo hxmlezblzvdbacebsmcb';

SET LOCAL ROLE apaar_migrator;
-- Do not default-grant every future table: that would expose Flyway history.
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM PUBLIC;
RESET ROLE;

DO $verify$
BEGIN
    IF has_schema_privilege('apaar_app','public','CREATE')
       OR has_database_privilege('apaar_app','postgres','CREATE')
       OR pg_has_role('apaar_app','apaar_migrator','MEMBER') THEN
        RAISE EXCEPTION 'STOP: application role has unexpected effective DDL privileges; entire bootstrap rolled back.';
    END IF;
END
$verify$;
COMMIT;

-- Non-secret result: both roles exist; no tables/data were created here.
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole, rolinherit,
       rolreplication, rolbypassrls
FROM pg_roles WHERE rolname IN ('apaar_app','apaar_migrator') ORDER BY rolname;
