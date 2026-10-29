# Evaluator & Invigilator Demonstration Guide
**Lease Document Approval Workflow — Enterprise Multi-Role Audit System**

This guide provides step-by-step instructions and demonstration scenarios for presenting the project to invigilators, evaluators, and technical examiners.

All sample files are located in the [`demo-documents/`](file:///d:/lease-document-approval-workflow/demo-documents/) directory.

---

## Sample Documents Overview

| File Name | Lease Category | Key Terms & Extracted Data | AI Risk Assessment | Key Features Demonstrated |
| :--- | :--- | :--- | :--- | :--- |
| **[`01_Commercial_Lease_Agreement_AcmeCorp.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/01_Commercial_Lease_Agreement_AcmeCorp.pdf)** | **Commercial** | Rent: **$4,500.00**<br>Deposit: **$9,000.00**<br>Term: **24 Months** | **LOW RISK (Green)** | Full Happy Path: Multi-stage Legal & Finance review, clean AI OCR extraction, Certificate of Approval generation. |
| **[`02_Retail_Lease_Flagged_Discrepancy.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/02_Retail_Lease_Flagged_Discrepancy.pdf)** | **Commercial** | Rent: **$2,800.00**<br>Deposit: *Unspecified*<br>Term: **12 Months** | **MEDIUM RISK (Warning)** | AI Discrepancy Detection: Detects "Residential Tenancy" clause inside Commercial lease, clause tagging, Request Changes, and Resubmission loop. |
| **[`03_Residential_Tenancy_Agreement_Skyline.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/03_Residential_Tenancy_Agreement_Skyline.pdf)** | **Residential** | Rent: **$1,850.00**<br>Deposit: **$2,500.00**<br>Term: **12 Months** | **LOW RISK (Green)** | Residential workflow, fast approval, per-persona notifications. |
| **[`04_Storefront_Floor_Plan_Addendum.png`](file:///d:/lease-document-approval-workflow/demo-documents/04_Storefront_Floor_Plan_Addendum.png)** | *Any* | Image / Architecture | N/A | Multi-file attachment: attach alongside a PDF to demonstrate multi-document validation and SHA-256 checksum tracking. |

---

## Scenario 1: The "Golden Happy Path" (Standard Commercial Lease)
> **Goal**: Show the invigilator the end-to-end workflow from submission to Legal approval, Finance underwriting, and final cryptographic Certificate of Approval.

### Step 1: Submit Lease Request as Tenant
1. Open the web app at `http://localhost:8081` (or click **New Submission** in the top navigation).
2. If not logged in, log in with default credentials or click Quick-Login:
   - **Persona**: **Prathmesh (Tenant / Requester)**
3. Navigate to **Submit Request** (`/submit-request`):
   - **Requester Name / ID**: `Acme Logistics Global (EMP-2026-ACME)`
   - **Lease Type**: `Commercial`
   - **File Upload**: Select [`01_Commercial_Lease_Agreement_AcmeCorp.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/01_Commercial_Lease_Agreement_AcmeCorp.pdf)
   - *(Optional)*: Also attach [`04_Storefront_Floor_Plan_Addendum.png`](file:///d:/lease-document-approval-workflow/demo-documents/04_Storefront_Floor_Plan_Addendum.png) to show multi-document ingestion.
4. Click **Submit Request & Documents**.
5. Note the generated **Request ID** (e.g. `#101`) and click **Track Request &rarr;**.

### Step 2: Show AI Document Intelligence & Tracking
- **Point out to the invigilator**:
  - The **Multi-Stage Approval Pipeline** shows Stage 1 (Submission) completed in emerald green.
  - The **Document Intelligence & Term Extraction** card automatically parsed:
    - **Extracted Rent**: `$4,500.00`
    - **Security Deposit**: `$9,000.00`
    - **Detected Duration**: `24 Months`
    - **Risk Assessment**: `LOW`
    - **Verified Covenants**: Indemnification clause verified, Default procedures verified.
  - Click **Inspect** or **View** on the document to demonstrate the embedded document preview modal with SHA-256 integrity checksum.

### Step 3: Legal Review Stage
1. Use the top-bar persona switcher to switch to **Legal Reviewer**.
2. Notice the **Notification Bell** increments with a new unread alert.
3. Open **Reviewer Queue** (`/reviewer-queue`).
4. Find the request in the queue and click **Approve Legal** (or open Details, add a verified clause note, then approve).
5. The request status advances to **`LEGAL_APPROVED`** and moves into the **Finance Audit** stage.

### Step 4: Finance Underwriting Stage & Certificate
1. Switch persona to **Finance Manager**.
2. In the **Reviewer Queue**, the button now displays **Approve Finance**.
3. Click **Approve Finance**.
4. The request status transitions to **`APPROVED`** (Stage 4 Completed).
5. Click **View Approval Certificate** (`/requests/{id}/certificate`) to show the official formal printable certificate with watermark, approval signatures, and audit trail!

---

## Scenario 2: AI Risk Detection, Reviewer Clause Tagging & Resubmission
> **Goal**: Demonstrate how the system detects risks and discrepancies, lets reviewers annotate clauses, and enables tenant resubmission.

### Step 1: Submit Retail Draft with Discrepancy
1. Switch to **Prathmesh (Tenant)**.
2. Go to `/submit-request`:
   - **Requester Name / ID**: `GreenLeaf Cafe (EMP-2026-GLC)`
   - **Lease Type**: `Commercial` *(Important: select Commercial)*
   - **Attach File**: [`02_Retail_Lease_Flagged_Discrepancy.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/02_Retail_Lease_Flagged_Discrepancy.pdf)
3. Submit and click **Track Request**.

### Step 2: Highlight AI Risk Warnings
- Point out to the evaluator:
  - **Risk Assessment**: Shows **`RISK ASSESSMENT: MEDIUM`** with warning badge.
  - **Items for Officer Attention**:
    - *"Discrepancy: Declared 'Commercial Lease' but document contains 'Residential Tenancy' clauses."*
    - *"Missing or non-standard Indemnification clause."*
    - *"Security deposit terms require manual officer verification."*

### Step 3: Officer Annotates Clause & Requests Changes
1. Switch persona to **Legal Reviewer** and open this request's details.
2. In the **Reviewer Clause Annotations & Defect Tags** section:
   - **Clause Tag**: `Section 9.2 Residential Tenancy Protection`
   - **Severity**: Select `DEFECT (Blocking Issue)`
   - **Note Text**: `Draft incorrectly imports Residential Tenancy covenants into a Commercial lease. Remove Section 9.2 and attach standard commercial indemnification.`
   - Click **Save Clause Annotation**. The defect note appears with a red badge!
3. Go back to `/reviewer-queue`, locate the request, and click **Request Changes**.
4. Enter reason: `Please remove residential tenancy clause and resubmit updated agreement.`
5. Click **Submit**.

### Step 4: Tenant Resubmits
1. Switch back to **Prathmesh (Tenant)** and navigate to `/track-request/{id}`.
2. The page shows **Action Required: Resubmit Request** in an amber box.
3. Attach the corrected file [`01_Commercial_Lease_Agreement_AcmeCorp.pdf`](file:///d:/lease-document-approval-workflow/demo-documents/01_Commercial_Lease_Agreement_AcmeCorp.pdf) and click **Resubmit Request & Files**.
4. The status transitions back to **`SUBMITTED`**, ready for re-review!

---

## Scenario 3: Negative Test Cases / Security & Validation
> **Goal**: If the evaluator asks *"What happens if a user uploads a virus, script, or 100MB file?"*

1. **Unsupported Extension**:
   - Try uploading a `.txt`, `.exe`, or `.zip` file on `/submit-request`.
   - The system displays: `"File '...' is of an unsupported format. Only PDF, JPG, and PNG files are allowed."`
2. **File Size Enforcement**:
   - Files exceeding 5 MB are rejected with explicit byte size validation.
3. **SHA-256 Cryptographic Checksum**:
   - Every file gets a SHA-256 hash computed on upload and displayed in the document table to guarantee file integrity.
