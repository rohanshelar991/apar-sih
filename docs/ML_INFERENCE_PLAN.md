# Notebook audit and separate inference integration plan

Status: notebook inspected, **no live ML inference integration**. No training,
model execution, dataset upload, or service deployment was performed in this task.

The supplied `APAAR_Anomaly_Detection_Full_Lifecycle.ipynb` contains 34 cells.
The notebook lives in the supplied local attachment path, not in the deployed
backend artifact. It uses pandas, NumPy, scikit-learn and joblib. It trains an
`IsolationForest` with 300 estimators, `contamination="auto"`, and a stratified
75/25 split of aggregate records. Labels are used for evaluation, not passed
to `.fit`. A row-level split alone does not establish performance on unseen
institutions or future academic years; assess those separately before release.

Its seven ordered inference features are:

1. `regular_utilization_pct`
2. `supernumerary_count`
3. `foreign_student_pct`
4. `missing_apaar_rate_pct`
5. `duplicate_apaar_rate_pct`
6. `yoy_change_pct`
7. `has_previous_year_data`

Missing year-on-year growth is filled with zero only alongside the explicit
historical-availability flag. Other missing/non-finite features are rejected.
The notebook exports `models/isolation_forest.joblib`, `feature_columns.json`,
held-out predictions and aggregate alert CSVs. `score_new_records` is a notebook
function, not an HTTP service. Saved notebook output is not evidence that the
current Java backend executes the model or that live metrics have been reproduced.

Current backend `IntakeService.getAnomalies` generates rule-derived duplicate,
intake-exceeded and shared-contact alerts. There is no Python inference endpoint,
model loader, ONNX runtime, or backend model-service client. Preserve these
existing rule alerts; do not label them model predictions.

## Proposed implementation, separate from this deployment

1. Reproduce training/evaluation with approved synthetic/de-identified data,
   versioned dependencies, fixed seeds and saved evaluation artifacts. Export
   the trusted model and ordered feature schema with checksums and model version.
   Never load an untrusted joblib/pickle upload: loading can execute code.
2. Define feature calculations in one shared specification, including admission
   year, counted statuses, categories, denominators, zero-capacity handling,
   missing history and units. Map Java `programme` to notebook `program`
   deliberately. Existing data/aggregates may not provide every feature; do not
   silently manufacture missing inputs or assume zero means unavailable.
3. Deploy a separate private Python inference service on Render, pinned to the
   validated pandas/scikit-learn/joblib versions. Load the bundled trusted model
   once at startup. No training at boot, no H2/SQLite/local-data dependency,
   no direct browser access and no additional authentication system for users.
4. Spring Boot performs institution authorization, computes approved aggregate
   features from the **new** Supabase demo DB, and invokes the private service
   with service-to-service authentication. Send aggregate numeric values and
   scoped identifiers only, not student names, APAAR IDs or contact details.
5. A proposed versioned batch contract returns `model_version`, aggregate IDs,
   `anomaly_prediction`, and `anomaly_score`. Notebook conventions are `-1` →
   anomaly and score `-decision_function`. Validate finite inputs, feature order,
   maximum batch size and response schema. Do not alter existing API contracts
   until a compatible additive endpoint/UI presentation is explicitly agreed.
6. Use bounded connection/read timeouts and clear unavailable status. Keep
   existing rule alerts available independently; never return a fabricated
   successful model score on timeout. Add deployment health/readiness checks.
7. Require offline-vs-service numerical parity tests, malformed input rejection,
   cross-institution authorization tests, service-auth tests, cold-start/load
   tests, and real end-to-end requests before marking ML LIVE. No such tests
   have been run yet. Student eligibility decisions must not be automated from
   an unvalidated anomaly score.

Remaining inputs: approved model/data artifacts, dependency versions, numerical
acceptance tolerances, aggregate feature definitions, privacy review, private
Render service authorization and resource budget.
