# APAAR GARUDA NETRA: Official Video Explanation Script & Recording Guide
**Smart India Hackathon 2026 | Problem Statement 26263**  
*AICTE Regulation Bureau | Theme: Smart Automation | Category: Software | Team: INFOBRO*

---

## Video Specifications
- **Target Video Duration**: 3 to 4 Minutes (Ideal for SIH judges evaluation)
- **Recommended Screen Resolution**: 1080p (1920x1080) at 60 FPS
- **Recording Software**: OBS Studio, Loom, or Windows Game Bar (`Win + G`)
- **Tone**: Professional, confident, high energy, engineering-oriented

---

## Scene-by-Scene Video Walkthrough

```
[0:00 - 0:30] Hook & Problem Statement
     ↓
[0:30 - 1:15] Institution Portal & Bulk Upload (PapaParse Preview)
     ↓
[1:15 - 1:55] 9-Rule Validation Engine & Inline Correction Workflow
     ↓
[1:55 - 2:45] AICTE National Regulator Dashboard & Anomaly Center
     ↓
[2:45 - 3:20] Student Privacy, QR Credential Card & Public Verification
     ↓
[3:20 - 3:45] Architecture, Honesty Rule & Closing Impact
```

---

### Scene 1: Introduction & The Problem (0:00 – 0:30)

#### On-Screen Action:
- Open browser at `http://localhost:5173/login`.
- Camera / Screen recording captures the polished AICTE-branded split layout, the Team INFOBRO badge, and the footer honesty badge.
- Hover mouse over the 3 one-click Demo Persona cards.

#### Voiceover Script:
> *"Respected judges and evaluators, welcome to our demonstration of **APAAR GARUDA NETRA**, developed by Team INFOBRO for Smart India Hackathon 2026, Problem Statement 26263 by the AICTE Regulation Bureau.*\
>  
> *Every year, over 10,000 AICTE-approved institutions admit millions of technical students. However, regulators face a critical blind spot: unverified student admissions, unauthorized intake exceedance breaches, cross-institutional duplicate enrolments, and privacy violations during manual credential verification.*  
>  
> *APAAR GARUDA NETRA solves this with an automated, 12-digit APAAR ID-based institutional registry featuring live anomaly intelligence, intake monitoring, and privacy-preserving QR verification. Let’s see it live."*

---

### Scene 2: Institution Portal & Ingestion Engine (0:30 – 1:15)

#### On-Screen Action:
1. Click the **"Institution Administrator"** card (`admin@bmsce.ac.in`).
2. Show the **Institution Overview** dashboard:
   - Point cursor at the 6 KPI cards: Total Students, Valid, Pending, Duplicates, Errors, and Intake Utilization.
   - Point out **B.Tech Computer Science (CSE)**: 115 students enrolled, approved intake 120 + 18 supernumerary (138 capacity). Currently status is healthy (**OK**).
3. Click **"Upload Admissions"** in the left sidebar.
4. Click the purple button: **"Load SIH Demo CSV (~60 rows)"** (or select `sample-uploads/new_admissions_INST001.csv`).
5. Show the **Client Preview** table parsed locally by PapaParse with verified green header mapping check.
6. Click **"Submit Batch for Server Validation"**.

#### Voiceover Script:
> *"We begin by logging in as the administrator of BMS College of Engineering. On our overview dashboard, we have real-time visibility into student counts and approved seat quotas. Notice that B.Tech Computer Science currently has 115 enrolments against a total permitted capacity of 138.*  
>  
> *Now, let’s navigate to 'Upload Admissions'. Colleges can drag-and-drop large CSV files up to 5,000 rows. The system parses the spreadsheet client-side with PapaParse, verifying column mapping before anything hits the network.*  
>  
> *Let's submit this batch of 60 new admissions to the backend server."*

---

### Scene 3: Validation Engine & Inline Correction (1:15 – 1:55)

#### On-Screen Action:
1. The animated progress bar reaches 100%, and confetti triggers!
2. Highlight the **Batch Results**:
   - `48 Valid Rows`
   - `2 Duplicates`
   - `10 Errors`
3. Point out row exceptions in the table:
   - **Cross-Institution Duplicate**: APAAR `202488339101` (*Kunal Deshmukh*) flagged because he is actively enrolled at **COEP Pune (INST002)** for the same academic year!
   - **In-File Duplicate**: APAAR `202699110022` (*Ayush Mathur*) submitted twice.
   - **Format & Missing Errors**: Invalid digit lengths (`123456`), non-digits (`APAAR778899`), missing DOB.
   - **Intake Exceeded**: BMSCE CSE enrolments pushed past 138 total permitted quota!
4. Click **"Validation Issues"** in the sidebar.
5. Click the dropdown arrow on a flagged record to show the full explanation and AICTE recommended fix.
6. Click **"Correct"**, update the demographic field, enter an audit note, and click **"Re-Validate & Resolve"**.
7. Show the success checkmark and the issue transitioning to `RESOLVED`.

#### Voiceover Script:
> *"Our asynchronous 9-rule Validation Engine analyzes the entire batch instantly against institutional constraints and simulated Academic Bank of Credits registries.*  
>  
> *Look at the intelligence at work:*  
> *First, it catches a **Cross-Institution Duplicate**: Student Kunal Deshmukh is already actively enrolled at COEP Pune in Maharashtra for this academic year—flagged without auto-deleting!*  
> *Second, it catches in-file duplicates and malformed APAAR numbers.*  
> *And third, adding these 35 students pushes BMSCE Computer Science past its sanctioned quota!*  
>  
> *If an administrator makes a typographical error, they don't re-upload the whole spreadsheet. They click 'Correct', rectify the record inline, add an audit note, and re-validate. Every single modification is immutably logged in `student_history` and `audit_logs`."*

---

### Scene 4: AICTE National Regulator Dashboard & Anomaly Center (1:55 – 2:45)

#### On-Screen Action:
1. Log out and click **"AICTE National Regulator"** (`regulator@aicte-india.org`).
2. Show the **National Dashboard**:
   - 10 Institutions, 3,600+ Enrolments, and Open Exceptions.
   - In the institution dropdown, select **"BMS College of Engineering (INST001)"**.
   - Show that **B.Tech CSE status has automatically transitioned to "EXCEEDED (>100%)"** at 108.7% utilization!
   - Point out the **Grouped Bar Chart** (Approved vs Supernumerary vs Actual) and the Category Donut chart.
3. Click **"Anomaly Center"** in the sidebar:
   - Show the flagged cross-institution duplicate for *Kunal Deshmukh*.
   - Click **"Escalate"**, type a note: *"Issued formal inquiry under AICTE Approval Process Handbook"*, and commit.
4. Click **"Summary Reports"**:
   - Click **"Download Summary PDF"** (open the downloaded PDF to show the official AICTE header, intake table, and exception log).
   - Click **"Download Summary CSV"**.
5. Click **"Audit Trail"** to show real-time chronological tracking of all logins, uploads, and decisions.

#### Voiceover Script:
> *"Now let's switch personas to the AICTE National Regulation Bureau. Regulators gain instant multi-institutional oversight without waiting months for manual paper filings.*  
>  
> *When we select BMS College of Engineering, look at the compliance table: B.Tech CSE has automatically turned to **EXCEEDED (108.7% utilization)** in real time! Regulators can compare sanctioned intake, supernumerary allowances, and category distribution at a glance.*  
>  
> *In the **Anomaly Center**, our cross-institutional algorithm has flagged the dual-enrolment conflict. The regulator can immediately choose to Review, Dismiss, or Escalate the case, sending an official inquiry with an immutable audit note.*  
>  
> *With one click, regulators can generate official signed PDF summary reports or download machine-readable CSV audits for regulatory committees."*

---

### Scene 5: Student Privacy & Public QR Code Verification (2:45 – 3:20)

#### On-Screen Action:
1. Navigate to **"Students Directory"** (`/institution/students`).
2. Click **"Verified QR Card"** on any student (e.g. *Aarav Sharma*).
3. In the modal:
   - Highlight the **Digital Consent Verification** check (DPDP Act compliance).
   - Show the **Data Minimization field toggles** (selecting only permitted fields).
   - Click **"Open Public Verifier"** (navigating to `/verify/:token`).
4. On the public verification page:
   - Show the authentic green checkmark: *"Authentic Student Credential"*, the timestamp, issuing authority, and **ONLY the permitted fields**.
   - Show the **Masked APAAR ID** (`20XX XXXX 0001`) preventing demographic leakage.
5. In the address bar or input box, change the token to `INVALID_TOKEN` to demonstrate graceful, tamper-evident error handling.

#### Voiceover Script:
> *"Finally, how do third parties like employers or higher education universities verify these credentials?*  
>  
> *In compliance with India's Digital Personal Data Protection (DPDP) Act, credential generation strictly checks for student consent. Institutions can choose exactly which fields to disclose—enforcing data minimization.*  
>  
> *The generated card features a secure QR code and a cryptographic token. When an employer scans this QR or visits the public verification route, the gateway displays an authentic green verification seal with ONLY the permitted fields. Sensitive APAAR identifiers are masked, preventing demographic data leakage. If a token is altered or expired, it fails gracefully."*

---

### Scene 6: Architecture & Conclusion (3:20 – 3:45)

#### On-Screen Action:
- Show Swagger UI at `http://localhost:8080/swagger-ui/index.html` or the Mermaid architecture diagram from the README.
- Return to the login hero screen.

#### Voiceover Script:
> *"Under the hood, APAAR GARUDA NETRA is built with a production-grade tech stack: Spring Boot 3 and Java on the backend, React 18, TypeScript, and Tailwind on the frontend, with zero-setup H2 in PostgreSQL mode, and full Docker containerization.*\
>  
> *Our interface-based adapter architecture ensures seamless plug-and-play integration with production DigiLocker and Academic Bank of Credits gateways in the future.*  
>  
> *APAAR GARUDA NETRA brings transparency, automation, and data integrity to technical education in India. Thank you!"*

---

## Pro Video Recording Tips

1. **Browser Setup**:
   - Use Google Chrome or Microsoft Edge in full-screen mode (`F11`).
   - Clear bookmarks bar or hide it (`Ctrl + Shift + B`) for a clean, distraction-free view.
   - Set browser zoom to 100% or 110% for crisp typography.
2. **Audio Setup**:
   - Use a decent headset or USB microphone.
   - Speak at an energetic, conversational pace (approx. 130–140 words per minute).
   - Eliminate background noise or enable noise suppression in OBS / Loom.
3. **Pacing**:
   - Pause for 1 second after each click so the viewer's eye can follow the UI transition before you speak about it.
