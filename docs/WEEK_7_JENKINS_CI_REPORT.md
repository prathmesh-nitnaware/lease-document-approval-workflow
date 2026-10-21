# Week 7 DevOps Practical Report: Continuous Integration with Jenkins Freestyle Project

**Course / Lab:** DevOps & Cloud Engineering Practicals (Week 7)  
**Project Title:** Lease Document Approval Workflow  
**Student / Developer:** Prathmesh Nitnaware  
**Repository:** `lease-document-approval-workflow`  
**GitHub URL:** [https://github.com/prathmesh-nitnaware/lease-document-approval-workflow](https://github.com/prathmesh-nitnaware/lease-document-approval-workflow)  
**Target Branch:** `develop`  
**CI Tool:** Jenkins (Freestyle Project)  
**Jenkins Job:** `lease-workflow-ci`  

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Practical Objectives](#2-practical-objectives)
3. [Project and CI Environment](#3-project-and-ci-environment)
4. [Jenkins Freestyle Configuration](#4-jenkins-freestyle-configuration)
   - 4.1 [Jenkins Installation & Service Context](#41-jenkins-installation--service-context)
   - 4.2 [GitHub Integration & SCM Configuration](#42-github-integration--scm-configuration)
   - 4.3 [SCM Polling Configuration](#43-scm-polling-configuration)
   - 4.4 [Build Environment & Credential Management](#44-build-environment--credential-management)
   - 4.5 [Maven Build Step Execution](#45-maven-build-step-execution)
   - 4.6 [Artifact Archiving & Fingerprinting](#46-artifact-archiving--fingerprinting)
5. [Security Handling & Sensitive Data Protection](#5-security-handling--sensitive-data-protection)
6. [CI Execution Process (Build #8)](#6-ci-execution-process-build-8)
7. [Automated Test Results](#7-automated-test-results)
8. [MySQL Database Integration Verification](#8-mysql-database-integration-verification)
9. [SCM Trigger Verification](#9-scm-trigger-verification)
10. [Artifact Generation and Archiving Verification](#10-artifact-generation-and-archiving-verification)
11. [Evidence Screenshots](#11-evidence-screenshots)
12. [Technologies and Versions Reference](#12-technologies-and-versions-reference)
13. [Challenges Encountered and Solutions](#13-challenges-encountered-and-solutions)
14. [Result and Evaluation](#14-result-and-evaluation)
15. [Conclusion](#15-conclusion)

---

## 1. Introduction

Continuous Integration (CI) is a foundational DevOps engineering discipline where software developers merge code changes into a central repository on a regular basis, after which automated builds and tests are executed. The primary goals of CI are to discover bugs and integration problems early, improve software quality, and significantly reduce the cycle time required to validate and publish deployable software packages.

In this Week 7 practical laboratory, a Continuous Integration pipeline was implemented for the **Lease Document Approval Workflow** application using **Jenkins**. The project is a multi-tier Spring Boot enterprise web application backed by a relational MySQL database and managed via Apache Maven. To achieve hands-on CI automation, a **Jenkins Freestyle Project** named `lease-workflow-ci` was configured to monitor the repository's `develop` branch, pull incoming source code automatically through SCM polling, compile the Java source code, run automated tests against a live MySQL schema, package the application into a Spring Boot executable JAR, and archive the resulting binary as a Jenkins build artifact.

This report comprehensively documents the architecture, configuration parameters, security safeguards, execution logs, and empirical evidence generated from the successful execution of **Build #8**.

---

## 2. Practical Objectives

The specific objectives for the Week 7 DevOps practical were as follows:

1. **SCM Integration:** Integrate Jenkins with the remote GitHub repository using secure token-based authentication.
2. **Automated Triggering via SCM Polling:** Configure periodic SCM polling on the active integration branch (`develop`) so that any pushed commit or merged pull request triggers an automated build without human intervention.
3. **Automated Compilation:** Trigger the Apache Maven build lifecycle to compile all Java 21 classes using standard compiler plugins.
4. **Automated Unit & Integration Testing:** Execute all JUnit 5 test suites during the CI build and enforce strict test passing criteria (zero failures, zero errors).
5. **Relational Database Connectivity:** Ensure that the CI runner successfully authenticates and establishes a persistent connection to the local MySQL instance (`lease_workflow`) to validate JPA entity persistence and repository queries during testing.
6. **Credential Masking & Security:** Prevent sensitive configuration and database credentials from being committed to source control by leveraging Jenkins Secret Text credentials and verifying `.gitignore` exclusions.
7. **Packaging & Artifact Archiving:** Package the validated Spring Boot application into an executable archive (`.jar`), archive the artifact within Jenkins, and record artifact fingerprints for traceability.

---

## 3. Project and CI Environment

The implementation utilized a dedicated, isolated build environment configured with the exact software toolchain versions detailed in Table 1.

### Table 1: Environment and Toolchain Specifications

| Component | Identifier / Specification | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Project Name** | `lease-document-approval-workflow` | `0.0.1-SNAPSHOT` | Spring Boot enterprise lease management service |
| **Source Repository** | `https://github.com/prathmesh-nitnaware/lease-document-approval-workflow` | Git | Remote version control repository |
| **Target Branch** | `develop` | N/A | Integration branch monitored by CI |
| **CI Automation Server** | Jenkins (Freestyle Project) | `2.568.3` | Continuous Integration orchestration engine |
| **Jenkins Job Name** | `lease-workflow-ci` | Freestyle | Dedicated build and test automation job |
| **Java Development Kit** | OpenJDK / HotSpot 64-Bit | `21.0.11` | Application runtime and compilation target |
| **Build Tool** | Apache Maven | `3.9.16` | Dependency resolution, compilation, testing, packaging |
| **Git Plugin** | `git` | `5.10.1` | Jenkins Git SCM provider plugin |
| **Git Client Plugin** | `git-client` | `6.6.1` | Low-level Git operations interface |
| **GitHub Plugin** | `github` | `1.47.0` | GitHub integration and webhook/polling hooks |
| **GitHub API Plugin** | `github-api` | `1.330-492.v3941a_032db_2a_` | Jenkins GitHub API communication library |
| **Maven Integration Plugin** | `maven-plugin` | `3.27` | Native Maven lifecycle support in Jenkins |
| **Database Server** | MySQL Community Server | `8.0.46` | Relational persistence backend (`lease_workflow`) |

---

## 4. Jenkins Configuration

The Jenkins Freestyle project `lease-workflow-ci` was configured through the Jenkins web interface using the following step-by-step specifications.

### 4.1 Jenkins Installation & Service Context

Jenkins version `2.568.3` is installed and executed as a dedicated Windows service under the service account `jenkins-svc`. The local build workspace is located at:

```text
C:\Users\jenkins-svc\AppData\Local\Jenkins\.jenkins\workspace\lease-workflow-ci
```

The necessary plugins (`git`, `git-client`, `github`, `github-api`, and `maven-plugin`) were verified in the Jenkins Plugin Manager to ensure compatibility with Java 21 and Maven 3.9.16.

### 4.2 GitHub Integration & SCM Configuration

The Source Code Management (SCM) section of `lease-workflow-ci` was configured to link directly with the remote GitHub repository:

- **SCM Type:** Git
- **Repository URL:** `https://github.com/prathmesh-nitnaware/lease-document-approval-workflow`
- **Credentials:** Jenkins GitHub Personal Access Token (PAT) credential with ID `github-pat`
- **Branch Specifier:** `*/develop`

The Jenkins Git plugin uses `GIT_ASKPASS` to authenticate against GitHub over HTTPS using the stored `github-pat` credential without exposing the token in workspace logs.

### 4.3 SCM Polling Configuration

To automate build triggering without requiring an inbound public webhook, Jenkins SCM polling was enabled under the **Build Triggers** section:

- **Trigger:** Poll SCM
- **Schedule Expression:**
  ```cron
  H/5 * * * *
  ```

**Operational Rationale:** The cron expression `H/5 * * * *` instructs Jenkins to poll the remote repository approximately every five minutes. The `H` (hash) symbol distributes the polling load across the Jenkins executor queue to avoid periodic server load spikes. During each polling cycle, Jenkins queries `git ls-remote` for `refs/heads/develop`. If the remote commit SHA differs from the last built revision, Jenkins queues a new build.

### 4.4 Build Environment & Credential Management

The application requires relational database credentials to initialize the Spring Data JPA persistence context during automated tests. Under Jenkins **Build Environment**, the option **"Use secret text(s) or file(s)"** was enabled:

- **Credential Type:** Secret Text
- **Credential ID:** `mysql-db-password`
- **Environment Variable Name:** `DB_PASSWORD`

**Security Rationale:** Hardcoding plaintext database passwords into configuration files or build scripts committed to Git exposes credentials to unauthorized repository viewers. By storing the password inside Jenkins's encrypted credential vault, the secret is only populated in process memory during build execution as `%DB_PASSWORD%`. Jenkins automatically masks this variable from the build console output.

### 4.5 Maven Build Step Execution

Because the Jenkins controller runs on Windows, an **Execute Windows batch command** build step was configured. This step sets the Spring datasource properties dynamically as environment variables before invoking Apache Maven:

```cmd
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lease_workflow
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%

mvn clean package

if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
```

**Build Step Breakdown:**
1. `set SPRING_DATASOURCE_URL=...`: Points the Spring Boot application to the local MySQL server and target schema (`lease_workflow`).
2. `set SPRING_DATASOURCE_USERNAME=root`: Provides the MySQL administrative username.
3. `set SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%`: Injects the database password from the Jenkins credential store.
4. `mvn clean package`: Deletes previous target artifacts, compiles the code, executes all JUnit tests, and packages the verified classes into an executable JAR.
5. `if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%`: Ensures that any compilation error or test failure causes the batch script to exit with a non-zero code, immediately failing the Jenkins build.

### 4.6 Artifact Archiving & Fingerprinting

To preserve deployable deliverables from successful builds, a **Post-build Action** was configured:

- **Action:** Archive the artifacts
- **Files to Archive:** `target/*.jar`
- **Fingerprinting:** Enabled ("Fingerprint all archived artifacts")

Fingerprinting computes an MD5 checksum of the generated JAR file, allowing Jenkins to track artifact usage and provenance across downstream jobs or deployments.

---

## 5. Security Handling & Sensitive Data Protection

A critical requirement of modern CI/CD engineering is the prevention of credential leakage into version control. In this project, local environment configurations and database passwords are stored strictly outside the Git tracking index.

### Verification of `.gitignore` Protection

The application configuration file `src/main/resources/application-local.yml` contains local environment connection strings and passwords. To verify that this file has never been tracked, committed, or exposed, three standard Git security verification commands were performed:

1. **Verify file is not currently tracked by Git:**
   ```bash
   git ls-files src/main/resources/application-local.yml
   ```
   *Result:* Returned **no output**, confirming that the file is not part of the current Git staging index or repository tree.

2. **Verify historical commit history:**
   ```bash
   git log --all -- src/main/resources/application-local.yml
   ```
   *Result:* Returned **no output**, confirming that the file was never committed in any prior branch, tag, or merge commit.

3. **Verify active `.gitignore` enforcement:**
   ```bash
   git check-ignore -v src/main/resources/application-local.yml
   ```
   *Result:* Output confirmed:
   ```text
   .gitignore:27:application-local.yml  src/main/resources/application-local.yml
   ```
   This proves that line 27 of `.gitignore` explicitly filters `application-local.yml`.

By enforcing this structure, database passwords remain entirely on the local machine and inside the Jenkins Credential Store, ensuring zero exposure in the remote GitHub repository.

---

## 6. CI Execution Process (Build #8)

On September 18, 2026, the complete automated CI lifecycle was validated through the execution of **Build #8**. The sequence of operations proceeded as follows:

```
+-----------------------------------------------------------------------------------+
| 1. SCM Change Detected: Remote head refs/heads/develop changed to d9dec03         |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 2. Job Triggered: Jenkins lease-workflow-ci Build #8 enqueued (SCM change)        |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 3. SCM Checkout: Checked out revision d9dec03 using credential 'github-pat'       |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 4. Environment Injection: DB_PASSWORD injected from Jenkins Secret Text store     |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 5. Compilation: Maven 3.9.16 (Java 21.0.11) compiled 16 main + 2 test classes     |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 6. Spring Boot Bootstrap & Testing: HikariCP connected to MySQL lease_workflow   |
|    - LeaseDocumentApprovalWorkflowApplicationTests (1 test passed)                |
|    - LeaseRequestServiceTest (6 tests passed)                                     |
|    - Total: 7 tests run, 0 failures, 0 errors, 0 skipped                          |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 7. Packaging: Spring Boot repackaged archive generated in target/                 |
|    - lease-document-approval-workflow-0.0.1-SNAPSHOT.jar (51.50 MiB)             |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 8. Post-Build: Archived JAR artifact, recorded fingerprints, Finished: SUCCESS    |
+-----------------------------------------------------------------------------------+
```

### Build Execution Summary

- **Trigger Cause:** Started by an SCM change
- **Revision Built:** `d9dec03a216f85ecf55002e13fe2ba681b5bcc0b`
- **Git Commit Message:** `docs: update README for Jenkins CI` (PR #8)
- **Executor Workspace:** `C:\Users\jenkins-svc\AppData\Local\Jenkins\.jenkins\workspace\lease-workflow-ci`
- **Compiler:** `javac [debug parameters release 21]`
- **Build Duration:** 24.819 seconds
- **Final Job Status:** `SUCCESS`

---

## 7. Automated Test Results

During the Maven `test` phase, the Surefire Plugin (`surefire:3.5.6`) invoked the JUnit 5 test platform. The test suite comprises two test classes validating application startup and business logic:

1. **`LeaseDocumentApprovalWorkflowApplicationTests`:**
   - **Type:** Spring Boot Integration Test (`@SpringBootTest`)
   - **Scope:** Bootstraps the full Spring application context, scans JPA repositories, initializes HikariCP connection pool, and verifies dependency injection.
   - **Execution Time:** 11.20 s
   - **Outcome:** Passed (1 test)

2. **`LeaseRequestServiceTest`:**
   - **Type:** Service Layer Unit & Business Logic Test
   - **Scope:** Tests state transitions, document submission, review assignments, approval logic, and validation constraints.
   - **Execution Time:** 1.430 s
   - **Outcome:** Passed (6 tests)

### Table 2: Automated Test Execution Breakdown

| Test Suite Class | Tests Executed | Failures | Errors | Skipped | Elapsed Time | Status |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| `LeaseDocumentApprovalWorkflowApplicationTests` | 1 | 0 | 0 | 0 | 11.20 s | **PASSED** |
| `LeaseRequestServiceTest` | 6 | 0 | 0 | 0 | 1.430 s | **PASSED** |
| **Total Test Summary** | **7** | **0** | **0** | **0** | **12.63 s** | **ALL PASSED** |

The Jenkins console output confirmed:

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 8. MySQL Database Integration Verification

The Lease Document Approval Workflow uses Spring Data JPA backed by Hibernate ORM 7.4.1.Final and HikariCP connection pooling. During the CI build, the application context successfully initialized a live database connection to MySQL.

The Jenkins build console log verified the following database connection parameters:

```text
2026-09-18T22:08:02.656+05:30  INFO 11072 --- [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [jdbc:mysql://localhost:3306/lease_workflow]
	Database driver: MySQL Connector/J
	Database dialect: MySQLDialect
	Database version: 8.0.46
	Default catalog/schema: lease_workflow/undefined
	Autocommit mode: undefined/unknown
	Isolation level: REPEATABLE_READ [default REPEATABLE_READ]
	JDBC fetch size: none
	Pool: DataSourceConnectionProvider
```

Additionally, the HikariCP connection log verified:

```text
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
```

This telemetry conclusively demonstrates that:
1. The Jenkins build environment correctly resolved the MySQL host at `localhost:3306`.
2. The MySQL user credentials passed via `%DB_PASSWORD%` were successfully authenticated.
3. The JPA schema and Hibernate dialect (`MySQLDialect`) were initialized without connectivity exceptions.

---

## 9. SCM Trigger Verification

To rigorously prove that the Jenkins CI pipeline operates automatically upon repository changes (and not via manual trigger), an end-to-end trigger verification test was conducted.

### 9.1 Verification Workflow

1. **Branch Protection Handling:** The GitHub repository enforces branch protection rules requiring all updates to `develop` to arrive through a Pull Request. Direct pushes to `develop` are rejected.
2. **Feature Branch Creation:** A temporary branch named `ci/scm-polling-test` was created.
3. **Harmless Documentation Commit:** A README-only update was committed to this branch with the commit message:
   ```text
   docs: update README for Jenkins CI
   ```
4. **Pull Request Submission & Merge:** Pull Request **#8** was submitted from `ci/scm-polling-test` into `develop` on GitHub. The PR was merged, producing merge commit `d9dec03a216f85ecf55002e13fe2ba681b5bcc0b`.
5. **Automated Detection via Polling:** Within five minutes, Jenkins SCM polling executed. The polling log (`polling.log`) recorded:

   ```text
   Started on Sep 18, 2026, 10:07:00 PM
   Using strategy: Default
   [poll] Last Built Revision: Revision e73ef2aaa8b1d1b6ea0f42c84c4b9a57afabd13a (refs/remotes/origin/develop)
   The recommended git tool is: NONE
   using credential github-pat
   ...
   Found 9 remote heads on https://github.com/prathmesh-nitnaware/lease-document-approval-workflow
   [poll] Latest remote head revision on refs/heads/develop is: d9dec03a216f85ecf55002e13fe2ba681b5bcc0b
   Done. Took 1.2 sec
   Changes found
   ```

6. **Automatic Build Initiation:** Jenkins immediately scheduled and started **Build #8**. The build status banner explicitly showed:
   ```text
   Started by an SCM change
   ```
   This confirms 100% automated triggering driven by source control changes.

---

## 10. Artifact Generation and Archiving Verification

Following the successful execution of the compilation and test lifecycle, Apache Maven invoked the `spring-boot-maven-plugin`:

```text
[INFO] --- jar:3.5.0:jar (default-jar) @ lease-document-approval-workflow ---
[INFO] Building jar: C:\Users\jenkins-svc\AppData\Local\Jenkins\.jenkins\workspace\lease-workflow-ci\target\lease-document-approval-workflow-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] --- spring-boot:4.1.0:repackage (repackage) @ lease-document-approval-workflow ---
[INFO] Replacing main artifact with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to lease-document-approval-workflow-0.0.1-SNAPSHOT.jar.original
```

In the post-build phase, Jenkins archived the generated JAR artifact to the controller's permanent storage:

- **Archived Artifact Name:** `lease-document-approval-workflow-0.0.1-SNAPSHOT.jar`
- **File Size:** `51.50 MiB` (`54,001,084 bytes`)
- **Fingerprinting:** MD5 fingerprint registered in Jenkins fingerprint database.

The **Build Artifacts** widget on the Jenkins Build #8 dashboard confirms the presence and size of the deployable artifact.

---

## 11. Evidence Screenshots

The empirical verification of the Jenkins CI pipeline is substantiated by the following captured evidence screenshots.

### Figure 1: Jenkins Build #8 Automatically Triggered by an SCM Change

The screenshot below demonstrates that Build #8 was automatically initiated by Jenkins SCM polling upon detecting the merged PR #8 commit on `refs/remotes/origin/develop`. The build dashboard explicitly displays the trigger cause: **"Started by an SCM change"** alongside the Git commit details.

![Figure 1: Jenkins Build #8 automatically triggered by an SCM change](file:///d:/lease-document-approval-workflow/docs/images/fig1-scm-trigger.png)

*Figure 1: Jenkins Build #8 status page displaying "Started by an SCM change", revision `d9dec03`, and the associated commit message.*

---

### Figure 2: Jenkins Console Output Showing Successful Tests, Packaging, and Artifact Archiving

The console output excerpt below shows the compilation, test execution (7 tests, 0 failures, 0 errors), Spring Boot JAR repackaging, Maven `BUILD SUCCESS`, artifact archiving, and final `SUCCESS` status.

![Figure 2: Jenkins console output showing successful tests, packaging, and artifact archiving](file:///d:/lease-document-approval-workflow/docs/images/fig2-console-build-success.png)

*Figure 2: Jenkins console log verifying that all 7 tests passed, the Spring Boot executable JAR was packaged, artifacts were archived, and the build completed with `SUCCESS`.*

---

### Figure 3: Successfully Archived Spring Boot JAR Artifact

The screenshot below displays the completed Jenkins Build #8 dashboard. It presents the **Build Artifacts** section showing `lease-document-approval-workflow-0.0.1-SNAPSHOT.jar` (51.50 MiB) while simultaneously corroborating that the build was **"Started by an SCM change"**.

![Figure 3: Successfully archived Spring Boot JAR artifact](file:///d:/lease-document-approval-workflow/docs/images/fig3-archived-artifact.png)

*Figure 3: Jenkins Build #8 summary displaying both the archived Spring Boot executable JAR artifact (51.50 MiB) and the SCM trigger provenance.*


---

## 12. Technologies and Versions Reference

Table 3 provides a consolidated audit of all technologies, tools, and plugins used throughout the Jenkins CI implementation.

### Table 3: Technology Stack and Exact Version Audit

| Tool / Technology | Version | Purpose in Pipeline |
| :--- | :--- | :--- |
| **Jenkins CI** | `2.568.3` | Core Continuous Integration automation server |
| **Java / JDK** | `21.0.11` | Compilation runtime, JVM platform for Spring Boot |
| **Apache Maven** | `3.9.16` | Dependency manager and build lifecycle execution |
| **Git Plugin** | `5.10.1` | SCM repository cloning, fetching, and revision tracking |
| **Git Client Plugin** | `6.6.1` | Low-level Git CLI abstraction for Jenkins agents |
| **GitHub Plugin** | `1.47.0` | GitHub project linking, webhooks, and repository synchronization |
| **GitHub API Plugin** | `1.330-492.v3941a_032db_2a_` | Client library for communicating with GitHub REST API |
| **Maven Integration Plugin** | `3.27` | Maven project build step and publisher integration |
| **MySQL Database** | `8.0.46` | Relational backend database (`lease_workflow`) |
| **MySQL Connector/J** | `9.x / 8.x` | JDBC database connectivity driver |
| **Hibernate ORM** | `7.4.1.Final` | JPA persistence provider and schema manager |
| **HikariCP** | `5.x / 6.x` | High-performance JDBC connection pool |
| **JUnit 5 / Surefire** | `3.5.6` | Automated unit and integration testing engine |

---

## 13. Challenges Encountered and Solutions

During the implementation of the Jenkins CI pipeline, three distinct technical challenges were encountered. Each challenge was diagnosed and resolved through systematic DevOps engineering practices.

### Challenge 1: Spring Boot Test Failure Due to Missing Datasource Configuration in Jenkins Workspace
- **Cause:** To prevent credential leakage, `application-local.yml` was excluded via `.gitignore`. When Jenkins performed a fresh checkout from the GitHub remote repository, `application-local.yml` was naturally absent from the workspace. Consequently, Spring Boot could not locate the datasource URL, username, or password, leading to test bootstrap failure.
- **Solution:** Instead of committing credentials to Git or placing files manually on the worker node, database configuration was supplied dynamically through Jenkins build-level environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`) combined with a securely stored Jenkins Secret Text credential (`mysql-db-password` exposed as `%DB_PASSWORD%`). Spring Boot automatically resolved these environment variables to populate its datasource properties.

### Challenge 2: MySQL Authentication Failure During Local Testing
- **Cause:** During initial local environment integration tests, the application failed to authenticate with the local MySQL server due to an incorrect database password configured in the local test properties.
- **Solution:** The credentials were verified against the running MySQL instance, updated in the Jenkins Secret Text store (`mysql-db-password`), and retested. Subsequent builds successfully established the connection, as confirmed by HikariCP log entries.

### Challenge 3: Direct Push to `develop` Rejected by GitHub Branch Protection
- **Cause:** Attempting to push the test commit directly to `develop` via Git CLI resulted in a rejection from GitHub, because repository branch rules require all changes to `develop` to pass through a pull request.
- **Solution:** A temporary branch `ci/scm-polling-test` was created, the commit was pushed to that branch, and Pull Request #8 was submitted and merged into `develop` on GitHub. Jenkins SCM polling subsequently detected the merged commit on `develop` and triggered Build #8 automatically.

---

## 14. Result and Evaluation

The factual evaluation of the Jenkins Freestyle CI implementation confirms that all practical requirements were met:

- **SCM Synchronization:** Successfully connected to GitHub and fetched the latest commit on `refs/heads/develop`.
- **Automated SCM Trigger:** Polling schedule `H/5 * * * *` automatically detected remote revision `d9dec03` and initiated Build #8 without manual intervention.
- **Secure Credential Injection:** Injected `%DB_PASSWORD%` from the Jenkins Credential Store without committing secrets to Git or exposing them in the build console.
- **Source Compilation:** Maven 3.9.16 compiled 16 main Java source files and 2 test source files under Java 21.0.11 without compiler warnings or errors.
- **Database Connection:** Established connection to `jdbc:mysql://localhost:3306/lease_workflow` on MySQL 8.0.46 via HikariCP.
- **Automated Tests:** Executed 7 tests across 2 test classes (`LeaseDocumentApprovalWorkflowApplicationTests` and `LeaseRequestServiceTest`), resulting in **0 failures, 0 errors, and 0 skipped tests**.
- **Packaging:** Assembled the Spring Boot executable archive `lease-document-approval-workflow-0.0.1-SNAPSHOT.jar`.
- **Artifact Archiving:** Successfully archived the 51.50 MiB JAR in Jenkins with fingerprinting enabled.
- **Final Build Status:** The build finished with `SUCCESS` in 24.819 seconds.

---

## 15. Conclusion

In this Week 7 DevOps practical, a complete, automated Continuous Integration pipeline was successfully constructed for the Spring Boot Maven application using a Jenkins Freestyle Project. The implementation demonstrated key principles of modern DevOps engineering:

1. **Automation:** Eliminating manual builds by monitoring version control branches through SCM polling.
2. **Quality Assurance:** Guaranteeing software correctness by running comprehensive unit and integration test suites on every incoming change.
3. **Security by Design:** Segregating sensitive infrastructure credentials from source code repositories through Jenkins credential management and `.gitignore` verification.
4. **Traceability:** Archiving validated build binaries with cryptographic fingerprints for reliable auditing and deployment readiness.

The empirical data, console logs, and evidence screenshots documented in this report confirm that the `lease-workflow-ci` pipeline fulfills all prescribed Week 7 practical objectives.
