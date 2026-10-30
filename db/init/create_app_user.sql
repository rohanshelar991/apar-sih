-- MANUAL administrator script, NOT a Flyway migration.
-- Run against a dedicated application database, before and after migration.
-- Then set passwords securely using psql: \password apaar_migrator / \password apaar_app
-- The first run creates roles; the second grants privileges on migrated tables.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'apaar_migrator') THEN
        CREATE ROLE apaar_migrator LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'apaar_app') THEN
        CREATE ROLE apaar_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
END $$;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO apaar_migrator;
-- Allows the provisioning administrator to grant privileges as the object owner.
GRANT apaar_migrator TO CURRENT_USER;
GRANT USAGE ON SCHEMA public TO apaar_app;
REVOKE CREATE ON SCHEMA public FROM apaar_app;

-- Grant only named application tables: never expose flyway_schema_history.
-- Re-run after future migrations introducing application tables/sequences.
SET ROLE apaar_migrator;
DO $$
DECLARE table_name text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY['institutions', 'programmes', 'students',
        'upload_batches', 'validation_issues', 'verification_tokens',
        'user_accounts', 'verification_requests', 'audit_logs', 'student_history']
    LOOP
        IF to_regclass('public.' || table_name) IS NOT NULL THEN
            EXECUTE format('GRANT SELECT, INSERT ON TABLE public.%I TO apaar_app', table_name);
            IF table_name IN ('audit_logs', 'student_history') THEN
                EXECUTE format('REVOKE UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON TABLE public.%I FROM apaar_app', table_name);
            ELSE
                EXECUTE format('GRANT UPDATE, DELETE ON TABLE public.%I TO apaar_app', table_name);
            END IF;
            EXECUTE format('GRANT USAGE ON SEQUENCE public.%I TO apaar_app', table_name || '_id_seq');
        END IF;
    END LOOP;
END $$;
RESET ROLE;

-- apaar_app must never own tables, inherit privileged roles, or receive PUBLIC DML.
-- Provision on a fresh database; this script does not sanitize pre-existing role memberships.
