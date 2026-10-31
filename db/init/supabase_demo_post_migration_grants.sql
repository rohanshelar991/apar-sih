-- REVIEW TEMPLATE ONLY. Run separately AFTER approved Flyway V1/V2 execution.
-- SQL Editor project MUST be hxmlezblzvdbacebsmcb; operator acknowledgement
-- below does not independently identify the server. Never run on the old project.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';
DO $preflight$
DECLARE
    confirmed_project text := '__CONFIRM_NEW_PROJECT_REF__';
    object_name text;
    object_owner oid;
BEGIN
    IF confirmed_project <> 'hxmlezblzvdbacebsmcb'
       OR current_database() <> 'postgres' OR current_user <> 'postgres' THEN
        RAISE EXCEPTION 'STOP: project confirmation or SQL Editor administrator context is incorrect.';
    END IF;
    PERFORM pg_advisory_xact_lock(262632027);
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='apaar_app')
       OR NOT EXISTS (SELECT FROM pg_roles WHERE rolname='apaar_migrator') THEN
        RAISE EXCEPTION 'STOP: approved bootstrap roles are missing.';
    END IF;
    IF EXISTS (SELECT FROM pg_roles WHERE rolname IN ('apaar_app','apaar_migrator')
        AND (rolsuper OR rolcreatedb OR rolcreaterole OR rolreplication OR rolbypassrls OR rolinherit OR NOT rolcanlogin))
       OR EXISTS (SELECT FROM pg_auth_members WHERE member='apaar_app'::regrole) THEN
        RAISE EXCEPTION 'STOP: unexpected role attributes or application memberships.';
    END IF;
    FOREACH object_name IN ARRAY ARRAY['institutions','programmes','students','upload_batches',
        'validation_issues','student_history','verification_tokens','audit_logs',
        'user_accounts','verification_requests','flyway_schema_history']
    LOOP
        SELECT relowner INTO object_owner FROM pg_class WHERE oid=to_regclass(format('public.%I',object_name));
        IF object_owner IS DISTINCT FROM 'apaar_migrator'::regrole::oid THEN
            RAISE EXCEPTION 'STOP: % is missing or not migrator-owned. No ownership changes will be made.',object_name;
        END IF;
    END LOOP;
    IF (SELECT count(*) FROM public.flyway_schema_history WHERE version IN ('1','2') AND success) <> 2
       OR EXISTS (SELECT FROM public.flyway_schema_history WHERE NOT success) THEN
        RAISE EXCEPTION 'STOP: successful V1/V2 migration history is required.';
    END IF;
END
$preflight$;

SET LOCAL ROLE apaar_migrator;
DO $grants$
DECLARE
    object_name text;
    identity_sequence text;
BEGIN
    FOREACH object_name IN ARRAY ARRAY['institutions','programmes','students','upload_batches',
        'validation_issues','student_history','verification_tokens','audit_logs',
        'user_accounts','verification_requests']
    LOOP
        EXECUTE format('REVOKE ALL PRIVILEGES ON TABLE public.%I FROM apaar_app',object_name);
        EXECUTE format('GRANT SELECT, INSERT ON TABLE public.%I TO apaar_app',object_name);
        IF object_name NOT IN ('audit_logs','student_history') THEN
            EXECUTE format('GRANT UPDATE, DELETE ON TABLE public.%I TO apaar_app',object_name);
        END IF;
        identity_sequence := pg_get_serial_sequence(format('public.%I',object_name),'id');
        IF identity_sequence IS NULL THEN RAISE EXCEPTION 'STOP: missing identity sequence on %',object_name; END IF;
        EXECUTE format('REVOKE ALL PRIVILEGES ON SEQUENCE %s FROM apaar_app',identity_sequence);
        EXECUTE format('GRANT USAGE ON SEQUENCE %s TO apaar_app',identity_sequence);
    END LOOP;
    REVOKE ALL PRIVILEGES ON TABLE public.flyway_schema_history FROM apaar_app;
END
$grants$;
RESET ROLE;

DO $verify$
DECLARE
    object_name text;
BEGIN
    IF has_schema_privilege('apaar_app','public','CREATE')
       OR has_database_privilege('apaar_app','postgres','CREATE')
       OR pg_has_role('apaar_app','apaar_migrator','MEMBER') THEN
        RAISE EXCEPTION 'STOP: application role can perform persistent DDL.';
    END IF;
    FOREACH object_name IN ARRAY ARRAY['institutions','programmes','students','upload_batches',
        'validation_issues','student_history','verification_tokens','audit_logs','user_accounts','verification_requests']
    LOOP
        IF NOT has_table_privilege('apaar_app','public.'||object_name,'SELECT')
           OR NOT has_table_privilege('apaar_app','public.'||object_name,'INSERT')
           OR has_table_privilege('apaar_app','public.'||object_name,'TRUNCATE')
           OR has_table_privilege('apaar_app','public.'||object_name,'TRIGGER')
           OR has_table_privilege('apaar_app','public.'||object_name,'REFERENCES')
           OR has_table_privilege('apaar_app','public.'||object_name,'UPDATE') <> (object_name NOT IN ('audit_logs','student_history'))
           OR has_table_privilege('apaar_app','public.'||object_name,'DELETE') <> (object_name NOT IN ('audit_logs','student_history')) THEN
            RAISE EXCEPTION 'STOP: unexpected effective privileges on %. Check inherited/PUBLIC grants; no automatic broad revocation.',object_name;
        END IF;
        IF NOT has_sequence_privilege('apaar_app',pg_get_serial_sequence(format('public.%I',object_name),'id'),'USAGE')
           OR has_sequence_privilege('apaar_app',pg_get_serial_sequence(format('public.%I',object_name),'id'),'UPDATE')
           OR has_sequence_privilege('apaar_app',pg_get_serial_sequence(format('public.%I',object_name),'id'),'SELECT') THEN
            RAISE EXCEPTION 'STOP: identity sequence privileges must be USAGE only on %.',object_name;
        END IF;
    END LOOP;
    IF has_table_privilege('apaar_app','public.flyway_schema_history','SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES,TRIGGER') THEN
        RAISE EXCEPTION 'STOP: application role can access Flyway history.';
    END IF;
END
$verify$;
COMMIT;

SELECT c.relname, pg_get_userbyid(c.relowner) AS owner
FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
WHERE n.nspname='public' AND c.relkind IN ('r','p') ORDER BY c.relname;
