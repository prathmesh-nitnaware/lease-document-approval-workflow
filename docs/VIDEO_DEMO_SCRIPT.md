# Automated Lease Document Approval Workflow
## Silent Video Demonstration Guide (No Spoken Audio Required)

> **Purpose**: A complete step-by-step visual recording guide designed for a **100% silent video submission**. All explanations, commands, and outputs are communicated through on-screen text banners, slides, terminal outputs, and UI interactions—requiring **zero voiceover or microphone**.

**Target Duration**: 4 to 6 Minutes  
**Video Resolution**: 1920x1080 (Full HD)  
**Recording Method**: Press `Win + Alt + R` in Windows. Ensure microphone is **MUTED** in the recording overlay.

---

## The Silent Storyboard Architecture

To keep the video engaging without speaking, use the following **Visual rhythm**:
1. **Show a Brief Title / Caption Slide (5–10 seconds)** introducing the upcoming milestone.
2. **Switch to Terminal or Browser (30–60 seconds)** to execute the action smoothly.
3. **Pause on the Output (3–5 seconds)** so the evaluator can clearly read the result (e.g. `BUILD SUCCESS`, `Status: UP`, `changed=0`).

```
+-----------------------------------------------------------------------------------------+
| SCENE TIMELINE (SILENT VIDEO FLOW)                                                      |
| Scene 1: Project Overview & Architecture (Slide & Web App Landing)       [0:00 - 0:40]  |
| Scene 2: Git Repository, Branches & Conventional Commits (Terminal)      [0:40 - 1:20]  |
| Scene 3: Automated Quality Gate & Maven Surefire Tests (Terminal)        [1:20 - 2:10]  |
| Scene 4: Docker Container Deployment & Actuator Health (Terminal)        [2:10 - 2:50]  |
| Scene 5: Live End-to-End Application Workflow (Browser)                  [2:50 - 4:40]  |
|          - Step A: Tenant Submission (Prathmesh + Acme Corp PDF)                        |
|          - Step B: AI Document Extraction & SHA-256 Hash Display                        |
|          - Step C: Legal Reviewer Queue & Legal Approval                                |
|          - Step D: Finance Underwriting & Final Approval                                |
|          - Step E: Cryptographic Certificate of Approval                                |
| Scene 6: Ansible IaC & Idempotency Proof (Terminal)                      [4:40 - 5:25]  |
| Scene 7: Project Sign-Off Slide (Conclusion)                             [5:25 - 5:45]  |
+-----------------------------------------------------------------------------------------+
```

---

## Detailed Step-by-Step Silent Execution

### Scene 1: Title & System Architecture (0:00 – 0:40)
- **On-Screen Display**:
  - Open `docs/demo_presentation.html` (Slide 1: Title & Architecture) OR show the application homepage at `http://localhost:8081/`.
  - Leave on screen for **10 seconds** so the title, your name (*Prathmesh Nitnaware*), and project scope are readable.
- **Visual Action**:
  - Scroll smoothly down the landing page to show:
    - Enterprise Dark Luxury branding
    - Multi-role personas: Tenant, Legal Reviewer, Finance Manager, Administrator
    - Key architectural features (AI Document Extraction, SHA-256 Tamper-Proofing, Quality Gate).

---

### Scene 2: Git Repository & Branching Governance (0:40 – 1:20)
- **Visual Context**: Open VS Code or Windows Terminal in `d:\lease-document-approval-workflow`.
- **Commands to Type / Run**:
  ```bash
  # 1. Show clean workspace and branch tracking
  git status

  # 2. Show active local and remote branches (GitFlow)
  git branch -a

  # 3. Show recent commit history with Conventional Commits and MVP tag
  git log --oneline --graph --decorate -n 6
  ```
- **Visual Pause**: Pause for **5 seconds** on the `git log` output so the examiner can see the commit messages:
  - `feat: ...`
  - `fix: ...`
  - `tag: v1.0.0-mvp`

---

### Scene 3: Automated Quality Gate & Continuous Testing (1:20 – 2:10)
- **Visual Context**: Terminal.
- **Commands to Run**:
  ```bash
  # Execute automated unit and headless Selenium acceptance tests
  mvn test
  ```
- **What Happens on Screen**:
  - Surefire starts ChromeDriver headless: `Starting ChromeDriver (headless=new)...`
  - Test suites execute cleanly:
    - `UiScreenshotCaptureTest` (5 UI acceptance journeys)
    - `PersonaServiceTest` (48 domain rules)
- **Visual Pause**: Highlight the final green summary block and pause for **5 seconds**:
  ```
  [INFO] Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  ```

---

### Scene 4: Docker Container Deployment & Health Telemetry (2:10 – 2:50)
- **Visual Context**: Terminal.
- **Commands to Run**:
  ```bash
  # 1. Verify running container and port mapping 8081:8080
  docker ps --filter name=lease-workflow-container

  # 2. Check live resource utilization (lightweight ~284 MB footprint)
  docker stats --no-stream lease-workflow-container

  # 3. Query the live Spring Boot Actuator health endpoint
  curl -i http://localhost:8081/actuator/health
  ```
- **Visual Pause**: Pause for **4 seconds** on the JSON response:
  ```json
  {"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
  ```

---

### Scene 5: Live End-to-End Application Walkthrough (2:50 – 4:40)
- **Visual Context**: Google Chrome / Edge browser at `http://localhost:8081/`.

#### Step 1: Submit Lease Request (Tenant Persona)
1. In the top navigation, ensure the persona badge displays **Prathmesh (Tenant / Requester)**.
2. Click **Submit Request** (`/submit-request`).
3. Fill the form smoothly:
   - **Requester Name**: `Acme Logistics Global (EMP-2026-ACME)`
   - **Lease Category**: `Commercial`
   - **Monthly Rent**: `4500`
   - **Duration**: `24 Months`
4. Drag and drop `demo-documents/01_Commercial_Lease_Agreement_AcmeCorp.pdf` into the upload zone.
5. Click **Submit Request & Documents**.
6. A success alert appears with Request ID `#101`. Click **Track Request &rarr;**.

#### Step 2: Show Document Intelligence & SHA-256 Seal
1. On the Tracking view (`/track-request/101`):
   - Hover cursor over the **Horizontal Stepper** (Stage 1 Completed in green).
   - Point cursor to **Document Intelligence Card**:
     - Extracted Rent: `$4,500.00`
     - Duration: `24 Months`
     - Risk Assessment: `LOW (Verified)`
   - Point cursor to **SHA-256 Checksum** to show cryptographic tracking.

#### Step 3: Legal Review Stage
1. Click the top navbar Persona switcher &rarr; select **Legal Reviewer**.
2. Click **Reviewer Queue** (`/reviewer-queue`).
3. Find request `#101` in the queue table.
4. Click the blue **Approve Legal** button.
5. Status badge updates to **`LEGAL_APPROVED`** in real time!

#### Step 4: Finance Underwriting Stage
1. Click the Persona switcher &rarr; select **Finance Manager**.
2. In the Reviewer Queue, the action button now displays **Approve Finance**.
3. Click the green **Approve Finance** button.
4. Status transitions to **`APPROVED`** (All stages completed).

#### Step 5: Official Approval Certificate
1. Click **View Approval Certificate** (`/requests/101/certificate`).
2. Scroll through the generated formal certificate:
   - Certificate Title: *Official Certificate of Lease Approval*
   - Gold border and seal
   - Signatures of Legal Reviewer & Finance Manager
   - **Cryptographic SHA-256 Verification Seal**: Highlight this on screen with the mouse cursor for **5 seconds**!

---

### Scene 6: Ansible IaC & Idempotency Proof (4:40 – 5:25)
- **Visual Context**: Terminal.
- **Commands to Run**:
  ```bash
  # Display the Ansible playbook architecture
  Get-ChildItem -Path d:\lease-document-approval-workflow\ansible -Filter "*.yml"

  # Show the idempotency proof command and output
  type docs/week14-idempotency-proof.txt
  ```
- **Visual Highlight**: Show the Play Recap proving zero drift:
  ```
  PLAY RECAP *******************************************************************
  target-node-01             : ok=8    changed=0    unreachable=0    failed=0
  ```
  *Pause on `changed=0` for **4 seconds** so the viewer sees zero drift.*

---

### Scene 7: Project Sign-Off Slide (5:25 – 5:45)
- **Visual Display**: Return to Slide Deck (`docs/demo_presentation.html` final slide) or show the root `README.md`.
- **On-Screen Text**:
  - *Project: Automated Lease Document Approval Workflow*
  - *15-Week DevOps Curriculum: Completed (Weeks 1 to 15)*
  - *Student / Lead: Prathmesh Nitnaware*
  - *Status: Ready for Viva & Evaluation*
- Hold on this screen for **5 seconds**, then press `Win + Alt + R` to stop recording.

---

## Helpful Recording Tips for a Silent Video

1. **Mouse Cursor Speed**: Move your mouse deliberately and smoothly. Avoid frantic or fast mouse shaking; pause for 2–3 seconds on important badges, buttons, and hash values so the viewer's eyes can follow.
2. **Text Zoom**:
   - In VS Code / Terminal: Press `Ctrl + =` once or twice so terminal commands are large and easily readable on any screen.
   - In the Browser: Set zoom to **100% or 110%**.
3. **No Audio Settings**:
   - In Windows Game Bar (`Win + G`), make sure the **Microphone** icon is crossed out (Muted).
   - This ensures a pure, silent MP4 file without accidental background keystrokes, breathing, or fan noise.
