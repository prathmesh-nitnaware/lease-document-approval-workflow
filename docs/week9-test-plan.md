# Week 9 Selenium WebDriver End-to-End Test Plan

## Overview
This document describes the automated end-to-end (E2E) UI testing suite implemented for the Lease Document Approval Workflow application. The suite is built with Selenium WebDriver 4.x, JUnit 5 Jupiter, and Spring Boot Test, executing in the standard Maven `test` phase (`mvn test`).

---

## Test Infrastructure & Architecture

- **Execution Phase**: Runs in the standard Maven test phase (`mvn test` with Surefire) alongside unit and service tests.
- **Spring Boot Lifecycle**: Leverages `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`. An isolated embedded Tomcat instance is spawned on an available dynamic port for each test class run, eliminating port collision risks and avoiding manual server start requirements.
- **Headless Execution**: Configured with Chrome headless mode by default (`--headless=new`, `--disable-gpu`, `--no-sandbox`, `--disable-dev-shm-usage`, `--window-size=1920,1080`) for clean CI/CD (Jenkins) execution. Browser visibility can be enabled for local debugging via `-Dselenium.headless=false`.
- **Automatic Driver Resolution**: Built on Selenium 4.28+ with native **Selenium Manager**, automatically resolving matching ChromeDriver binaries without external third-party dependencies like `webdrivermanager`.
- **Screenshot on Failure**: Single-capture JUnit 5 extension (`ScreenshotOnFailureExtension` implementing `AfterTestExecutionCallback`) captures full browser screenshots on test failure to `target/selenium-screenshots/<testClassName>_<testMethodName>.png` before driver teardown.
- **Driver Teardown**: Ensured after each test execution via `AfterEachCallback` / `quitDriver()`.
- **Test-Data Isolation & Cleanup**: Dynamic UUID-based requester IDs prevent collision. In addition, `BaseSeleniumTest` tracks all created request IDs and cleans up child records (`Document`, `ReviewAction`, `StatusHistory`) and parent `LeaseRequest` records in `@AfterEach` teardown, preventing database pollution across repeated runs.

---

## Critical User Journeys Tested

| # | Journey Name | Test Class | Primary Objective & Workflow | Key Assertions | Test Data & Fixtures |
|---|--------------|------------|------------------------------|----------------|----------------------|
| **1** | **Submit Valid Lease Request** | `SubmitValidLeaseRequestTest` | Fill out requester name and lease type, attach valid PDF document, and submit via `/submit-request`. | 1. Confirmation card (`#confirmation-section`) is displayed.<br>2. Unique Request ID (`#confirmation-request-id`) is generated and visible.<br>3. Status badge shows `SUBMITTED`.<br>4. Navigating to `/track-request/{id}` confirms status `SUBMITTED` and requester name.<br>5. Persistence verification via `LeaseRequestService`. | - Requester Name: `Requester_<UUID>`<br>- Lease Type: `Commercial`<br>- Fixture: `sample-lease-doc.pdf` (valid ~1KB PDF) |
| **2** | **Submit Invalid Document Type** | `SubmitInvalidDocumentTest` | Attempt to upload an unpermitted file format (`.txt`) via `/submit-request`. | 1. Inline validation error alert (`#error-alert`, `#error-message`) is displayed.<br>2. Confirmation section is NOT rendered.<br>3. Database verification confirms NO request record was created for the requester name. | - Requester Name: `InvalidUser_<UUID>`<br>- Lease Type: `Residential`<br>- Fixture: `invalid-type.txt` (plain text) |
| **3** | **Submit Oversized Document** | `SubmitOversizedDocumentTest` | Attempt to upload a document exceeding the 5MB upload limit via `/submit-request`. | 1. Inline validation error alert (`#error-alert`, `#error-message`) is displayed.<br>2. Error text confirms maximum file size violation (5 MB limit).<br>3. Confirmation section is NOT rendered.<br>4. Database verification confirms NO request record was created. | - Requester Name: `OversizedUser_<UUID>`<br>- Lease Type: `Commercial`<br>- Fixture: `oversized-file.pdf` (>6MB PDF) |
| **4** | **Reviewer Approves Request** | `ReviewerApprovesRequestTest` | Directly seed a request in `SUBMITTED` status, navigate to `/reviewer-queue`, locate the item, and click `Approve`. | 1. Reviewer queue row (`#request-row-{id}`) is located.<br>2. Success banner (`#success-alert`) confirms approval.<br>3. Request status transitions to `APPROVED` in database/service.<br>4. Row is removed from the pending reviewer queue table. | - Seeded Request: `ApproverTest_<UUID>`<br>- Lease Type: `Commercial`<br>- In-memory PDF document |
| **5** | **Reviewer Rejects with Mandatory Comment** | `ReviewerRejectsWithCommentTest` | Directly seed a request, verify server-side rejection on blank comment, verify UI modal HTML5 validation, then submit rejection with mandatory reason comment. | 1. Server-side rejection confirms `IllegalArgumentException` on blank comment.<br>2. Empty comment triggers client HTML5 validation and blocks submission.<br>3. Status remains `SUBMITTED` when comment is omitted.<br>4. Submitting with reason updates status to `REJECTED`.<br>5. Rejection comment is retrievable and visible on `/track-request/{id}` (`#review-comment-text`). | - Seeded Request: `RejectTest_<UUID>`<br>- Lease Type: `Equipment`<br>- In-memory PDF document<br>- Comment: `"Missing counter-signature on section 4.B"` |
| **6** | **Request Changes & Requester Resubmission** | `ChangesRequestedThenResubmitTest` | Reviewer requests revisions with feedback note; requester opens `/track-request/{id}`, views the resubmit card, attaches revised files, and resubmits. | 1. Reviewer action transitions status to `CHANGES_REQUESTED`.<br>2. Tracking view displays `CHANGES_REQUESTED` and renders `#resubmit-section`.<br>3. Uploading revised document via `#resubmitFileInput` and clicking `#resubmit-btn` succeeds.<br>4. Status transitions back to `SUBMITTED` on page and in database. | - Seeded Request: `ResubmitTest_<UUID>`<br>- Lease Type: `Industrial`<br>- Revision Note: `"Please upload signed appendix C..."`<br>- Resubmit Fixture: `sample-lease-doc.pdf` |
| **7** | **Invalid Resubmission Rejected** | `ResubmitInvalidDocumentTest` | Reviewer requests changes; requester attempts to resubmit using an unpermitted/invalid file (`invalid-type.txt`). | 1. Validation error is displayed (`#error-alert`, `#error-message`).<br>2. Status remains `CHANGES_REQUESTED` and does NOT become `SUBMITTED`.<br>3. Database status verified to preserve `CHANGES_REQUESTED`.<br>4. Resubmission card remains accessible for re-attempt. | - Seeded Request: `InvalidResubmit_<UUID>`<br>- Lease Type: `Commercial`<br>- Resubmit Fixture: `invalid-type.txt` |

---

## Screenshot on Failure Mechanism

- **Implementation**: `ScreenshotOnFailureExtension` implements `AfterTestExecutionCallback`.
- **Trigger**: Executed when `context.getExecutionException()` is present.
- **Output Artifact**: Stored as `target/selenium-screenshots/<TestClassName>_<TestMethodName>.png`.
- **Deduplication**: Uses a single capture point immediately upon test method failure before browser driver shutdown or database teardown.

---

## Test Data Isolation & Cleanup Architecture

- **Registration**: Tests register created request IDs via `registerCreatedRequest(id)` and unique requester names via `registerCreatedRequesterId(name)`.
- **Teardown**: `@AfterEach` teardown in `BaseSeleniumTest` removes test data in child-first order:
  1. `Document` records matching the request ID
  2. `ReviewAction` audit records matching the request ID
  3. `StatusHistory` transition records matching the request ID
  4. `LeaseRequest` root record
- **Scope Safety**: Only records generated by the executing test are targeted; unrelated production or baseline records are preserved.

---

## Test Fixture Directory (`src/test/resources/test-data/`)

1. `sample-lease-doc.pdf`: Valid placeholder PDF file for successful submissions and resubmissions (~1KB).
2. `oversized-file.pdf`: Large dummy PDF file (6.29MB) exceeding the 5MB upload validation threshold.
3. `invalid-type.txt`: Plain text document for asserting file extension/mime-type rejection.

---

## Selector Stabilization Summary

All UI elements targeted by automated tests have explicit, stable ID attributes added across templates:

- **`submit-request.html`**: `#requesterId`, `#leaseType`, `#fileInput`, `#submit-btn`, `#confirmation-section`, `#confirmation-request-id`, `#confirmation-status`, `#confirmation-doc-table`, `#error-alert`, `#error-message`.
- **`reviewer-queue.html`**: `#reviewer-queue-table`, `#request-row-{id}`, `#req-id-{id}`, `#req-requester-{id}`, `#req-type-{id}`, `#req-status-{id}`, `#approve-btn-{id}`, `#changes-btn-{id}`, `#reject-btn-{id}`, `#commentModal`, `#modalComment`, `#modalSubmitBtn`, `#modalCancelBtn`, `#success-alert`, `#error-alert`.
- **`track-request.html`**: `#track-request-id`, `#status-badge`, `#track-requester-id`, `#track-lease-type`, `#track-submitted-date`, `#track-doc-table`, `#review-actions-section`, `#review-comment-text`, `#resubmit-section`, `#resubmitFileInput`, `#resubmit-btn`, `#success-alert`, `#error-alert`.
