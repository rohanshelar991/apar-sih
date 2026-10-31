# Security changes and remaining release checks

The new Supabase project is `hxmlezblzvdbacebsmcb`. No connection or writes to it
have been performed. The retired production-labelled project is blocked before
non-development PostgreSQL startup, and no old-project tests were run during
cloud preparation. Live deployment is not claimed.

## Implemented and regression-tested

- Central institution authorization runs in student, upload, issue-resolution
  and token-issuance services. Administrators cannot supply another institution,
  read/edit another institution's students, export its records, or change a
  student/issue to a foreign programme. Missing institution associations fail
  closed. Regulator cross-institution access remains available.
- Institution lists/programme lists/statistics are scoped. Validation issues use
  explicit left joins so manual and batch-only issues remain visible; a mixed
  institution issue cannot expose a foreign student's data through an owned batch.
- Consent updates are persisted, with issued tokens deactivated in the same
  transaction on withdrawal. Re-consent does not reactivate those tokens. Public
  verification independently rechecks current consent for legacy active tokens
  and does not return student fields when consent is absent.
- Missing consent is not silently granted by manual creation or CSV import.
  CSV callers may explicitly supply `consent_given=true`; operators remain
  responsible for having actual consent. The manual form starts unchecked.
- JWT signature/expiry validation is retained. Each JWT-backed request also
  checks the database account is active and its role/institution still match.
  Missing, disabled and role-changed accounts cannot reuse their old tokens.
- Public `/api/health` uses a read-only connection check and returns only UP/DOWN.
  CORS supports Authorization and multipart/JSON preflight, allows only configured
  origins, and cloud startup rejects local/wildcard CORS configuration.
- Uploads require a non-empty CSV filename, retain the 20 MB multipart limit,
  and roll back on checked as well as unchecked processing failures.
- Cloud startup requires a new project match, TLS, Flyway, Hibernate validation,
  no H2/dev profiles, and a Base64 JWT key of at least 48 bytes. Runtime logs do
  not print JWT validation exception details or request bodies/access logs.

Regression fixtures run only in explicitly selected disposable test databases;
they are not production fallbacks or Supabase account provisioning. The test
suite exercises real BCrypt login and JWT/profile filtering in that fixture.

## Remaining limits — do not call this a completed production security audit

- New-project connectivity, database grants, migrations, all 10 mappings and
  real persona logins remain NOT RUN pending explicit authorization and credentials.
- Hosted ingress/edge rate limits for public login and verification endpoints,
  TLS/custom domains, secret storage permissions, backups and monitoring need
  infrastructure configuration and validation. No hosting accounts are connected.
- Logout clears browser tokens; there is no per-token server-side logout denylist
  or refresh-token lifecycle. An otherwise valid stolen token remains usable
  until expiry unless the account/role/institution is changed or disabled.
- Input/data validation still uses the existing nine-rule engine. A broad
  adversarial validation, CSV formula-export review, dependency vulnerability
  scan and load test have not been completed. Filename checks are not proof of
  safe CSV contents. Production uploads need resource/transaction load testing.
- Opaque verification tokens are stored in the existing token table, and the
  issuance audit path retains the original token-as-entity-ID behavior. Anyone
  legitimately able to read token records or those audit entries may obtain a
  bearer verification capability. Masking/hashing stored tokens requires a
  separately designed compatible change and migration, not an unreviewed schema edit.
- Existing institution/programme APIs provide listing and metrics; dedicated
  institution/programme CRUD management endpoints are not implemented merely
  because repository CRUD exists. An empty new project cannot accept useful
  programme-based admissions until authorized setup supplies programmes.
- Existing anomaly alerts are rule-based; notebook inference is not live.
  See [the separate ML integration plan](ML_INFERENCE_PLAN.md).

These limits require review before public release with real student data. The
deployment workflow stays gated until the owner has supplied the new target,
configured provider permissions, and completed the remaining release checks.
