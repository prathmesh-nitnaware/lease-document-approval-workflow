# Automated Lease Document Approval Workflow

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg?style=flat&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4%2B-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg?style=flat&logo=docker)](https://www.docker.com/)
[![Jenkins CI/CD](https://img.shields.io/badge/Jenkins-Pipeline%20as%20Code-red.svg?style=flat&logo=jenkins)](https://www.jenkins.io/)
[![Ansible](https://img.shields.io/badge/Ansible-Idempotent%20Provisioning-EE0000.svg?style=flat&logo=ansible)](https://www.ansible.com/)
[![DevSecOps](https://img.shields.io/badge/DevSecOps-Trivy%20%7C%20SAST%20%7C%20SHA--256-blueviolet.svg?style=flat&logo=shield)](https://github.com/aquasecurity/trivy)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An enterprise-grade, cloud-native automated lease document processing and multi-stage underwriting platform. Built on **Java 21 LTS** and **Spring Boot**, the platform orchestrates contract intake, automated AI covenant extraction, multi-role review workflows, interactive cryptographic e-signatures, and immutable audit certification. Engineered strictly under modern **DevOps and SRE principles**, complete with Jenkins Continuous Delivery, Docker lifecycle governance, and Ansible automated configuration management.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
   - [Industry Background](#industry-background)
   - [Key Challenges & Pain Points](#key-challenges--pain-points)
   - [Target Impact & Goals](#target-impact--goals)
2. [Proposed Solution](#2-proposed-solution)
   - [End-to-End Orchestrated Pipeline](#end-to-end-orchestrated-pipeline)
   - [Dual-Stage Review Protocol](#dual-stage-review-protocol)
   - [AI Document Intelligence & Covenant Engine](#ai-document-intelligence--covenant-engine)
   - [Enterprise Cryptographic Certification](#enterprise-cryptographic-certification)
3. [Technical Architecture & Specifications](#3-technical-architecture--specifications)
   - [System Architecture Diagram](#system-architecture-diagram)
   - [Backend Core](#backend-core)
   - [Data & Storage Tier](#data--storage-tier)
   - [Frontend & Presentation Layer](#frontend--presentation-layer)
   - [DevOps, CI/CD & Infrastructure Stack](#devops-cicd--infrastructure-stack)
   - [DevSecOps & Threat Mitigation](#devsecops--threat-mitigation)
4. [System Personas & Access Control](#4-system-personas--access-control)
5. [Core Features & Functional Modules](#5-core-features--functional-modules)
   - [Document Ingestion & SHA-256 Integrity](#feature-1-document-ingestion--sha-256-integrity)
   - [AI Document Intelligence & Risk Scoring](#feature-2-ai-document-intelligence--risk-scoring)
   - [Cryptographic Certificate of Approval & E-Signatures](#feature-3-cryptographic-certificate-of-approval--e-signatures)
   - [Reviewer Queue, Clause Annotations & Resubmission](#feature-4-reviewer-queue-clause-annotations--resubmission)
   - [Audit Trail & Executive KPI Analytics](#feature-5-audit-trail--executive-kpi-analytics)
6. [DevOps Curriculum & 15-Week Timeline Mapping](#6-devops-curriculum--15-week-timeline-mapping)
7. [Installation & Deployment Guide](#7-installation--deployment-guide)
   - [Prerequisites](#prerequisites)
   - [Local Development Setup](#local-development-setup)
   - [Docker Containerized Execution](#docker-containerized-execution)
   - [Jenkins Automated CI/CD Deployment](#jenkins-automated-cicd-deployment)
   - [Ansible Idempotent Server Provisioning](#ansible-idempotent-server-provisioning)
8. [Quality Assurance & Automated Testing](#8-quality-assurance--automated-testing)
9. [Branching Convention & Git Workflow](#9-branching-convention--git-workflow)
10. [Verification Evidence & System Health](#10-verification-evidence--system-health)

---

## 1. Problem Statement

### Industry Background
Commercial real estate and corporate property leasing involve extensive, legally binding documentation comprising commercial lease agreements, indemnification covenants, financial escrow guarantees, property floor plans, and statutory disclosures. In traditional enterprise environments, lease underwriting is executed manually through fragmented email threads, paper signatures, and disconnected spreadsheet logs.

### Key Challenges & Pain Points
1. **Prolonged Processing Latency & SLA Violations**:
   Manual handoffs between prospective tenants, legal counsel, and commercial underwriting managers introduce latency averaging 14 to 28 days per contract, leading to lost leasing opportunities and operational friction.
2. **High Human Error in Covenant Verification**:
   Lease contracts contain critical statutory covenants—such as *Indemnification & Bilateral Liability Allocation*, *Notice of Default / Cure Periods*, *Governing Law Jurisdiction*, and *Subleasing Restrictions*. Human reviewers frequently overlook missing clauses or category mismatches (e.g. residential tenancy clauses submitted in commercial filings).
3. **Absence of Cryptographic Document Integrity**:
   Standard PDF uploads and emailed scans lack tamper-evident integrity mechanisms. Altered financial figures (e.g., base rent, security deposit escrow) or modified liability clauses can go unnoticed across review iterations.
4. **Lack of Transparent Auditability**:
   Fragmented communication prevents real-time tracking. Tenants are blind to their application state, while compliance auditors cannot establish an immutable chronological record of who reviewed, amended, or approved specific clauses.
5. **Deployment Inconsistencies & Operational Fragility**:
   Legacy deployments suffer from "environmental drift"—discrepancies in JVM patches, filesystem permissions, and database configurations between developer workstations and staging servers.

### Target Impact & Goals
- Reduce lease approval cycle times from weeks to under 24 hours.
- Automate clause extraction and multi-factor compliance risk scoring (0–100) before human review.
- Establish an immutable cryptographic audit trail with SHA-256 document hashing and digital signatures.
- Provide 100% reproducible, automated deployments via Docker containerization and Ansible configuration management.

---

## 2. Proposed Solution

The **Automated Lease Document Approval Workflow** provides a centralized, cloud-ready digital platform that completely automates the lease intake, review, and certification lifecycle:

```
[ Tenant / Requester ]
        │
        ▼ (Uploads PDF Contract & Metadata)
┌────────────────────────────────────────────────────────┐
│  Multi-Document Ingestion & SHA-256 Checksum Engine   │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│  AI Document Intelligence & Automated Risk Engine      │
│  - Extracts Rent, Deposit, Duration, Governing Law     │
│  - Verifies Indemnification & Cure Periods             │
│  - Generates Multi-Factor Risk Score (0-100)           │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼ (Transitions: SUBMITTED)
┌────────────────────────────────────────────────────────┐
│  Stage 1: Legal Review & Compliance Clearance         │
│  - Clause Defect Annotation & Tagging                  │
│  - Action: APPROVE / REJECT / REQUEST_CHANGES          │
└───────────────────────┬────────────────────────────────┘
                        │ (Transitions: LEGAL_APPROVED)
                        ▼
┌────────────────────────────────────────────────────────┐
│  Stage 2: Finance & Commercial Underwriting           │
│  - Escrow Validation & Payment Schedule Cross-Check    │
│  - Action: APPROVE / REJECT                            │
└───────────────────────┬────────────────────────────────┘
                        │ (Transitions: APPROVED)
                        ▼
┌────────────────────────────────────────────────────────┐
│  Cryptographic Certificate of Approval & E-Signatures  │
│  - Master SHA-256 Digest (RFC 6234 Compliant)          │
│  - Dual Interactive Digital Signature Canvases         │
│  - Immutable Audit Certificate (Print / PDF Export)    │
└────────────────────────────────────────────────────────┘
```

### Key Solution Highlights
- **Automated AI Pre-Check**: Instantly scans uploaded PDFs using Apache PDFBox and NLP regex heuristics, highlighting discrepancies and categorizing contracts into **`LOW`**, **`MEDIUM`**, or **`HIGH`** risk bands before reviewers open the file.
- **Dual-Gate Governance**: Enforces strict two-stage separation of duties: Legal Counsel must clear statutory compliance before Finance can underwrite commercial terms.
- **Digital E-Signature Studio**: An interactive HTML5 canvas allowing reviewers to draw signatures or adopt verified cursive scripts, secured by browser persistence and tamper-evident visual stamps.
- **Master Cryptographic Seal**: Binds the request ID, document hashes, reviewer identities, and approval timestamps into a single SHA-256 fingerprint, guaranteeing audit immutability.
- **Turnkey DevOps Pipeline**: Managed under strict Gitflow, with parameterized Jenkins Continuous Deployment, zero-downtime Docker container recycling, and automated Ansible provisioning.

---

## 3. Technical Architecture & Specifications

### System Architecture Diagram
```
                             CLIENT TIER
                 ┌────────────────────────────────┐
                 │ Responsive Web UI (Thymeleaf)  │
                 │ Bootstrap 5 + Luxury Dark CSS  │
                 │ Chart.js KPI Dashboard         │
                 │ HTML5 E-Signature Pad (Canvas) │
                 └───────────────┬────────────────┘
                                 │ HTTP / REST / JSON
                                 ▼
                           SECURITY LAYER
                 ┌────────────────────────────────┐
                 │ Spring Security 6 RBAC Gate    │
                 │ In-Memory & Database Personas  │
                 │ Session Governance & CSRF Opt. │
                 └───────────────┬────────────────┘
                                 │
                     APPLICATION / SERVICE TIER
                 ┌────────────────────────────────┐
                 │ LeaseRequestService            │
                 │ DocumentIntelligenceService    │
                 │ CertificateService             │
                 │ AnnotationService              │
                 │ FileStorageService (SHA-256)   │
                 │ NotificationService            │
                 │ ValidationService              │
                 └───────────────┬────────────────┘
                                 │
                            DATA ACCESS
                 ┌────────────────────────────────┐
                 │ Spring Data JPA / Hibernate 7  │
                 │ Repository Pattern (6 Repos)   │
                 └───────────────┬────────────────┘
                                 │
                   PERSISTENCE & STORAGE TIER
         ┌───────────────────────┴───────────────────────┐
         ▼                                               ▼
┌─────────────────────────────────┐   ┌─────────────────────────────────┐
│ H2 Database Engine (Local/Test) │   │ MySQL 8.0 Enterprise (Prod)     │
│ Zero-config in-memory operation │   │ Persistent schema & constraints │
└─────────────────────────────────┘   └─────────────────────────────────┘
         │                                               │
         └───────────────────────┬───────────────────────┘
                                 ▼
                 ┌────────────────────────────────┐
                 │ Persistent File Storage Volume │
                 │ Docker Volume: `lease-uploads` │
                 │ /app/uploads/documents/        │
                 └────────────────────────────────┘
```

### Detailed Tech Stack Specifications

| Layer / Concern | Technology | Version | Purpose / Rationale |
| :--- | :--- | :--- | :--- |
| **Language Runtime** | Java OpenJDK | `21 LTS` | Modern virtual threads, pattern matching, record patterns, high-performance runtime. |
| **Core Framework** | Spring Boot | `3.4.x` / `4.x` | Enterprise application foundation, dependency injection, auto-configuration. |
| **Security & Auth** | Spring Security | `6.x` | Role-Based Access Control (`TENANT`, `LEGAL_REVIEWER`, `FINANCE_MANAGER`, `ADMIN`). |
| **Persistence (ORM)** | Spring Data JPA / Hibernate | `7.x` | Declarative repositories, transactional management, automated DDL generation. |
| **Document Parsing** | Apache PDFBox | `3.0.4` | Server-side text stripping, PDF metadata extraction, and OCR tokenization. |
| **Embedded Database** | H2 Database | `2.4.x` | Lightweight, zero-dependency in-memory database for developer speed and unit testing. |
| **Production Database** | MySQL Community Server | `8.0` | ACID-compliant relational persistence, foreign key integrity, connection pooling. |
| **Template Engine** | Thymeleaf | `3.1.x` | Server-rendered HTML5 with seamless Spring Security integration. |
| **Frontend Styling** | Bootstrap & Theme Luxury | `5.3.x` | Custom Dark Luxury design system (`theme-luxury.css`), high contrast, CSS grid. |
| **Data Visualization** | Chart.js | `4.4.x` | Interactive doughnut and bar charts for dashboard KPI tracking. |
| **Signature Capture** | HTML5 Canvas 2D API | Native | High-DPI anti-aliased freehand drawing and programmatic cursive font adoption. |
| **Container Engine** | Docker Engine & Buildx | `26.x+` | Immutable container builds based on hardened `eclipse-temurin:21-jre`. |
| **Continuous Integration** | Jenkins CI/CD | `2.440+ LTS` | Declarative Pipeline-as-Code (`Jenkinsfile`) with automated SCM polling triggers. |
| **Config Management** | Ansible | `2.16+` | Declarative, idempotent target host provisioning (`playbook.yml`, `vault.yml`). |
| **Reverse Proxy** | Nginx | `1.24+` | Host-level reverse proxy, SSL termination, and static asset acceleration. |
| **Automated Testing** | JUnit 5 & Mockito | `5.10.x` | Comprehensive unit testing and test-double isolation. |
| **E2E UI Testing** | Selenium WebDriver | `4.28.1` | Headless Chrome automated user journey execution with screenshot capture. |
| **Observability** | Spring Boot Actuator | `3.4.x` | Health status (`/actuator/health`), readiness/liveness probes, system metrics. |

---

## 4. System Personas & Access Control

The platform implements strict Role-Based Access Control (RBAC) ensuring separation of duties across all workflow touchpoints:

| Persona Name | Role Identifier | Access Scope & Responsibilities | Demo Credentials |
| :--- | :--- | :--- | :--- |
| **Prathmesh** | `TENANT` | Prospective Tenant. Submits lease applications, uploads PDF contracts, monitors live status on tracker, views and prints final approval certificates. | `prathmesh` / `password123` |
| **Legal Reviewer** | `LEGAL_REVIEWER` | Legal Counsel. Inspects statutory clauses, flags contract defects with annotations, executes Stage 1 clearance (**`APPROVE`** / **`REJECT`** / **`REQUEST_CHANGES`**), and signs certificate. | `sarah.legal` / `password123` |
| **Finance Manager** | `FINANCE_MANAGER` | Commercial Underwriter. Reviews rental economics, verifies security deposit escrow, executes Stage 2 final approval, and co-signs certificate. | `marcus.finance` / `password123` |
| **Administrator** | `ADMIN` | System Governance & Operations. Full access to executive analytics dashboard, reviewer queues, system health checks, and CSV data export. | `admin` / `password123` |

---

## 5. Core Features & Functional Modules

### Feature 1: Document Ingestion & SHA-256 Integrity
- Multi-file intake supporting PDF, DOCX, and PNG formats up to 50 MB.
- Generates a cryptographic SHA-256 checksum immediately upon receipt:
  $$\text{SHA-256} = \text{MessageDigest}(\text{DocumentBytes})$$
- Stores files under isolated, UUID-prefixed file paths (`/app/uploads/documents/req-X_UUID_filename.pdf`).
- Displays document fingerprint badges across tracker and reviewer queue interfaces.

### Feature 2: AI Document Intelligence & Risk Scoring
- Automated text extraction using **Apache PDFBox 3.0.4** with resilient stream-fallback parsing.
- Clause Recognition Engine extracts:
  - **Monthly Base Rent**: Identifies values from standard financial clauses (e.g. `Monthly Rent: $3,500.00`).
  - **Security Deposit**: Identifies escrow obligations (e.g. `Security Deposit: $7,000.00`).
  - **Lease Term**: Calculates duration (e.g. `24 Months`).
  - **Governing Law**: Extracts legal jurisdiction (e.g. *State of New York*, *California*, *Delaware*).
  - **Notice Period**: Discovers termination cure periods (e.g. *60 Days Written Notice*).
  - **Subleasing Restrictions**: Classifies sublet covenants (*Prohibited*, *Landlord Consent Required*, *Permitted*).
  - **Indemnification Clause**: Validates presence of bilateral liability shields.
- **Dynamic Multi-Factor Compliance Score (0–100)**:
  - Starts at baseline $100$.
  - Deducts weighted penalties for category discrepancies ($-25$), missing indemnification ($-15$), undefined default remedy ($-15$), or unparsed financial terms ($-10$).
  - Classifies application into **`LOW RISK`** ($\ge 80$), **`MEDIUM RISK`** ($55–79$), or **`HIGH RISK`** ($< 55$).
- Renders a real-time visual score gauge, animated progress bar, and 6-point metrics grid on `track-request.html`.

### Feature 3: Cryptographic Certificate of Approval & E-Signatures
- Generates an immutable **Certificate of Lease Approval** for all `APPROVED` contracts (`/requests/{id}/certificate`).
- **Cryptographic Master Seal**:
  Computes an RFC 6234-compliant SHA-256 digest binding the Certificate Number, Request ID, Signer Names, Document Hashes, and Issue Timestamp.
- **Interactive E-Signature Canvas Studio**:
  - High-DPI HTML5 canvas supporting freehand touch/mouse drawing with smooth bezier curve interpolation.
  - "Adopt Script" button programmatically renders an executive cursive script signature.
  - "Lock & Seal" button secures the signature, applies a tamper-evident visual watermark stamp, and persists the vector data in `localStorage` across page reloads and browser sessions.
  - Clean print engine (`@media print`) ensures the certificate, cryptographic seal, and signatures print crisply to paper or PDF while hiding interactive buttons.

### Feature 4: Reviewer Queue, Clause Annotations & Resubmission
- Centralized reviewer workbench (`/reviewer-queue`) featuring live status filtering (**`ALL`**, **`SUBMITTED`**, **`UNDER_REVIEW`**, **`CHANGES_REQUESTED`**, **`APPROVED`**, **`REJECTED`**).
- Real-time text search across Requester IDs and Lease Categories.
- Modal-based document inspection preview iframe with direct SHA-256 checksum verification.
- **Clause Defect Annotation Engine**: Reviewers can attach targeted clause tags (e.g., *Clause 4.1 Indemnity*) with severity classifications (**`INFO`**, **`WARNING`**, **`CRITICAL`**).
- Resubmission workflow allows tenants to upload amended documents, automatically resetting review stages.

### Feature 5: Audit Trail & Executive KPI Analytics
- Chronological status history ledger recording every transition with actor name and microsecond timestamp.
- Executive KPI dashboard (`/dashboard`) powered by Chart.js displaying:
  - Total Volume, Active In-Review, Approval Rate %, and Average Processing Latency.
  - Visual status distribution doughnut chart.
- One-click CSV Data Export (`/api/export/csv`) generating structured operational audit logs.

---

## 6. DevOps Curriculum & 15-Week Timeline Mapping

This project was developed incrementally over a 15-week DevOps curriculum, implementing professional engineering and operational practices at each phase:

| Phase / Week | Milestone Topic | Key Deliverables & Practical Artifacts |
| :---: | :--- | :--- |
| **Week 1** | **Problem Definition & Scope** | Problem statement, stakeholder analysis, functional requirements, success metrics. |
| **Week 2** | **Agile Planning & DevOps Workflow** | Jira/Agile backlog, user stories with acceptance criteria, sprint roadmap. |
| **Week 3** | **Requirements & Architecture** | SRS document, 3-tier Spring Boot architecture, REST endpoint catalog, Maven setup. |
| **Week 4** | **Git & GitHub Initialization** | Repository setup, `.gitignore`, Gitflow strategy (`main`, `develop`, `feature/*`). |
| **Week 5** | **Feature Development with Branching** | Core domain entities, file upload handler, JPA repositories, feature branching. |
| **Week 6** | **MVP Completion & Collaboration** | Multi-role review states (`LEGAL_APPROVED`, `APPROVED`), peer code review, PR merges. |
| **Week 7** | **Jenkins CI Pipeline** | Local Jenkins server setup, SCM polling triggers, automated Maven build verification. |
| **Week 8** | **Pipeline as Code & CD** | Declarative `Jenkinsfile` with parameterization (`BRANCH_NAME`, `APP_PORT`, `SPRING_PROFILE`). |
| **Week 9** | **Selenium Test Automation** | Headless Chrome E2E journey tests (`UiScreenshotCaptureTest`), failure screenshot capture. |
| **Week 10** | **Continuous Testing in Jenkins** | JUnit test reporting integration, quality gate threshold (0 test failures enforced). |
| **Week 11** | **Docker Image & Lifecycle** | Multi-stage `Dockerfile` (`eclipse-temurin:21-jre`), container lifecycle management. |
| **Week 12** | **Jenkins-Docker CD Pipeline** | Automated CD pipeline: build JAR &rarr; build Docker image &rarr; push to Docker Hub &rarr; deploy container. |
| **Week 13** | **Configuration Management (Ansible)** | Declarative Ansible playbooks (`setup_lease_server.yml`), user creation, Docker setup. |
| **Week 14** | **Automated Provisioning & Reliability** | Ansible idempotency verification (`changed=0`), Actuator health verification, rollback plan. |
| **Week 15** | **Final Release, Documentation & Viva** | Comprehensive system manual, video demonstration script, slide presentation, viva prep. |

---

## 7. Installation & Deployment Guide

### Prerequisites
- **Java Development Kit (JDK)**: OpenJDK 21 LTS installed and on `PATH`.
- **Apache Maven**: Version 3.9.0 or higher.
- **Docker Engine**: Version 24.0+ with Docker Compose.
- **Git**: Version 2.40+.
- **Ansible** *(Optional for SRE deployment)*: Version 2.15+.

### Local Development Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/prathmesh-nitnaware/lease-document-approval-workflow.git
   cd lease-document-approval-workflow
   ```

2. **Build and Package**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   *The application starts on `http://localhost:8081` using the embedded H2 in-memory database.*

4. **Run Automated Unit & Integration Tests**:
   ```bash
   mvn test
   ```

---

### Docker Containerized Execution

1. **Build the Docker Image**:
   ```bash
   docker build -t lease-workflow-app:latest .
   ```

2. **Run the Application Container**:
   ```bash
   docker run -d \
     --name lease-workflow-container \
     -p 8081:8081 \
     -v lease-uploads:/app/uploads/documents \
     lease-workflow-app:latest
   ```

3. **Verify Container Health**:
   ```bash
   curl -i http://localhost:8081/actuator/health
   ```
   *Expected Response:*
   ```json
   {"groups":["liveness","readiness"],"status":"UP"}
   ```

4. **Container Lifecycle Commands**:
   ```bash
   # Inspect running logs
   docker logs --tail 50 -f lease-workflow-container

   # Check resource metrics
   docker stats --no-stream lease-workflow-container

   # Stop and restart container
   docker stop lease-workflow-container
   docker start lease-workflow-container
   ```

---

### Jenkins Automated CI/CD Deployment

The repository includes a battle-tested Declarative `Jenkinsfile`:

- **Pipeline Parameters**:
  - `BRANCH_NAME`: Branch to build (`develop`, `main`).
  - `APP_PORT`: Exposed host port (`8081`).
  - `SPRING_PROFILE`: Target profile (`dev`, `staging`, `prod`).
- **Pipeline Stages**:
  1. **Checkout**: Retrieves parameterized branch via Git credentials.
  2. **Build**: Terminates conflicting processes and runs `mvn clean package`.
  3. **DevSecOps Security Audit**: Executes static secret detection, scans for hardcoded tokens, and verifies dependency tree integrity.
  4. **Package**: Archives build artifacts (`target/*.jar`).
  5. **Docker Build**: Builds versioned image tag (`${BUILD_NUMBER}-${GIT_SHA}`) and `latest`.
  6. **Container Security Scan**: Scans Docker image for HIGH/CRITICAL CVEs via Trivy.
  7. **Docker Push**: Authenticates to Docker Hub and pushes versioned image tags.
  8. **Deploy**: Recycles existing container and launches the fresh image with persistent volumes.
  9. **Health Verification**: Asserts HTTP 200 on `/actuator/health`.

---

### Ansible Idempotent Server Provisioning

For production server orchestration, Ansible playbooks are located in `ansible/`:

1. **Configure Inventory & Vault**:
   ```bash
   # Update target node in inventory
   vi ansible/inventory/hosts.ini

   # Configure encrypted database credentials
   ansible-vault edit ansible/group_vars/vault.yml
   ```

2. **Execute Provisioning Playbook**:
   ```bash
   ansible-playbook -i ansible/inventory/hosts.ini ansible/playbook.yml
   ```

3. **Verify Idempotency (Zero Changes on Re-Run)**:
   ```bash
   ansible-playbook -i ansible/inventory/hosts.ini ansible/playbook.yml
   ```
   *Verified Result:*
   ```
   PLAY RECAP *********************************************************************
   lease-target-node   : ok=26   changed=0    unreachable=0    failed=0    skipped=2
   ```

---

## 8. Quality Assurance & Automated Testing

The project implements a comprehensive testing pyramid ensuring defect-free releases:

```
          / \
         /   \     E2E UI Journeys (Selenium WebDriver)
        /     \    Headless Chrome, Screenshot Capture
       /-------\
      /         \   Integration Tests (Spring Boot Test)
     /           \  WebMvcTest, Repository Data Access
    /-------------\
   /               \ Unit Tests (JUnit 5 + Mockito)
  /                 \ Business Logic, AI Regex, Checksums
 /-------------------\
```

### Test Suite Summary
- **`DocumentIntelligenceServiceTest`**: Validates PDF text parsing, monthly rent and security deposit regex matching, lease term discovery, and category discrepancy alert generation.
- **`CertificateServiceTest`**: Validates certificate data generation, status enforcement (requires `APPROVED`), and SHA-256 seal computation.
- **`FileStorageServiceTest`**: Asserts deterministic SHA-256 hash generation, file byte storage, and duplicate upload handling.
- **`UiScreenshotCaptureTest`**: Headless Selenium test automating the end-to-end journey from submission to approval certificate generation, archiving PNG evidence to `target/screenshots/`.

Execute all tests with:
```bash
mvn clean test
```

---

## 9. Branching Convention & Git Workflow

The project enforces the standard **Gitflow Branching Model**:

```
[main] ─────────────────────────────────────────────● (v1.0 Release)
          ▲                                         │
          │ Release PR                              ▼
[develop] ─┴──────●────────────────────────●────────● (Integration)
                   ▲                      ▲
      Feature PR   │         Feature PR   │
[feature/*] ───────┴────────             ──┴────────
```

- **`main`**: Production-ready, strictly deployable release branch.
- **`develop`**: Central integration branch for ongoing sprint deliverables.
- **`feature/<name>`**: Isolated feature branches branched from `develop` and merged via Pull Requests.
- **`fix/<name>`**: Surgical defect and bugfix branches.

---

## 10. Verification Evidence & System Health

| Verification Item | Command / URL | Expected Proof |
| :--- | :--- | :--- |
| **Application Actuator** | `http://localhost:8081/actuator/health` | `HTTP 200 OK` &rarr; `{"status":"UP"}` |
| **Track Request UI** | `http://localhost:8081/track-request/1` | AI Compliance Card, 100/100 score, extracted terms. |
| **Certificate View** | `http://localhost:8081/requests/1/certificate` | SHA-256 Master Seal, interactive dual signature pads. |
| **Reviewer Queue** | `http://localhost:8081/reviewer-queue` | Queue table, status chips, modal document preview. |
| **Docker Status** | `docker ps -f name=lease-workflow-container` | `Status: Up (healthy)`, port `0.0.0.0:8081->8081/tcp`. |
| **Ansible Idempotency** | `ansible-playbook ...` | `changed=0`, `failed=0`. |

---

## Author & Project Metadata
- **Author**: Prathmesh Nitnaware
- **Academic / Course Focus**: Comprehensive DevOps, Continuous Integration, Continuous Delivery, and Site Reliability Engineering Curriculum (Weeks 1–15).
- **Repository**: [prathmesh-nitnaware/lease-document-approval-workflow](https://github.com/prathmesh-nitnaware/lease-document-approval-workflow)

---

### Thank You!
Thank you for reviewing and utilizing the Automated Lease Document Approval Workflow platform.

