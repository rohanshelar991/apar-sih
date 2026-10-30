# APAAR-Connect: 3-Minute Hackathon Demo Script
**Smart India Hackathon 2026 | Problem Statement 26263**
*AICTE Regulation Bureau | Theme: Smart Automation | Category: Software | Team: INFOBRO*

---

## Quick Reference Demo Credentials
| Persona / Role | Institution Context | Email | Password |
|---|---|---|---|
| **Institution Admin** | BMS College of Engineering (INST001) | `admin@bmsce.ac.in` | `Admin@123` |
| **AICTE Regulator** | AICTE National Regulation Bureau | `regulator@aicte-india.org` | `Regulator@123` |
| **Authorized Verifier** | Tata Consultancy Services (TCS) | `verifier@tcs.com` | `Verifier@123` |

---

## 3-Minute Click-by-Click Walkthrough

### Minute 0:00 - 0:45 | Login & The Institution Portal
1. Open **[http://localhost:5173](http://localhost:5173)** in the browser.
2. Highlight the SIH 2026 / AICTE branding, the 3 one-click demo login cards, and the **"Prototype – simulated APAAR verification"** honesty badge in the footer.
3. Click the **"Institution Administrator"** card (`admin@bmsce.ac.in`).
4. You land on the **Institution Overview**:
   - Point out the 6 KPI metrics: Total Students (~1,000+), Valid, Pending, Duplicates, Errors, and Intake Utilization.
   - Note the **B.Tech Computer Science & Engineering (CSE)** programme: currently seeded with 115 students out of a total permitted intake of 138 (120 approved + 18 supernumerary quota). Status is currently healthy (**OK**).

### Minute 0:45 - 1:45 | Bulk Ingestion & The 9-Rule Validation Engine
1. Click **Upload Admissions** in the left sidebar.
2. Click **"Load SIH Demo CSV (~60 rows)"** (or select `sample-uploads/new_admissions_INST001.csv`).
3. Show the instant **Client-Side Preview** powered by PapaParse with verified column mapping.
4. Click **"Submit Batch for Server Validation"**.
5. Watch the real-time validation progress bar run against the simulated APAAR adapter.
6. When complete, show the result breakdown:
   - **Valid Rows**: Processed & compliant.
   - **Cross-Institution Duplicate**: APAAR `202488339101` flagged because the student is actively enrolled at **COEP Pune (INST002)** for the same academic year!
   - **In-File Duplicate**: APAAR `202699110022` flagged for appearing twice in the batch.
   - **Format & Demographic Errors**: Flagged for invalid digit lengths (`123456`), non-numeric characters (`APAAR778899`), missing DOB, and simulated unknown APAAR (`999900001111`).
   - **Intake Exceeded**: Enrolling 35 CSE students pushed total BMSCE CSE enrolments to 150 > 138!
7. Navigate to **Validation Issues**:
   - Filter by `OPEN` and `DUPLICATE_CROSS_INSTITUTION`.
   - Click the expand arrow on a row to show the exact rule violated and the AICTE recommended fix.
   - Click **"Correct"** on a flagged record, fix the demographic typo, add a resolution note, and click **"Re-Validate & Resolve"**.
   - Show how the issue resolves instantly, recording immutable entries in both `student_history` and `audit_logs`.

### Minute 1:45 - 2:30 | AICTE Regulator Oversight & Anomaly Center
1. Log out and click the one-click **"AICTE National Regulator"** card (`regulator@aicte-india.org`).
2. You land on the **National Regulator Dashboard**:
   - Observe national KPIs: 10 Institutions, ~1,500+ Enrolments, and Open Exceptions.
   - Select **"BMS College of Engineering (INST001)"** in the dropdown.
   - Point out that **B.Tech CSE has turned into "INTAKE EXCEEDED (>100%)"** in the compliance table!
   - Show the **Approved vs Actual Enrolment** grouped bar chart and the Category distribution donut.
3. Click **Anomaly Center** in the left sidebar:
   - Show the cross-institution dual-enrolment anomaly for student *Kunal Deshmukh*.
   - Click **"Escalate"** or **"Review"**, enter a regulatory note, and commit the decision.
4. Click **Summary Reports**:
   - Click **"Download Summary PDF"** to open an official signed PDF report generated live by OpenPDF.
   - Click **"Download Summary CSV"** for data export.
5. Click **Audit Trail** to show complete chronological logging of all uploads, edits, and decisions.

### Minute 2:30 - 3:00 | Student Privacy & Public QR Code Verification
1. Navigate to **Students Directory** (`/institution/students`).
2. Find any verified student (e.g., *Aarav Sharma*) and click **"Verified QR Card"**.
3. Point out the **Digital Consent Verification** check (DPDP Act compliance) and the **Data Minimization** field selector (permitting only authorized fields).
4. Click **"Open Public Verifier"** (or scan the QR code, opening `/verify/:token`):
   - The public gateway renders an authentic green checkmark, the issuing authority, and **ONLY the permitted fields**.
   - Masked APAAR ID: `20XX XXXX 3344` preserves student privacy.
5. Demonstrate error resilience: enter an invalid or truncated token in `/verify` to see the clear, graceful error state.

---

## Judge Q&A Talking Points
- **Zero-Setup Demo**: Starts with 1 command (`start-demo.bat`) using embedded H2 in PostgreSQL mode. Production Docker Compose is included for real PostgreSQL.
- **Simulated APAAR Adapter**: Interface-based design allows swapping the synthetic generator for production DigiLocker/ABC APIs with zero changes to business logic.
- **Security & Privacy**: Role-Based Access Control (RBAC) on every endpoint, BCrypt password hashing, JWT stateless tokens, and data minimization at verification.
