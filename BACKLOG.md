# Product Backlog & Traceability Matrix (MVP Baseline v0.1.0-mvp)

All MVP user stories (US1 through US9) have been implemented, tested, reviewed, and merged into the `develop` branch.

## Story Completion Summary

| Story ID | Story Title | Status | Branch | Implementing Commits / PR |
|---|---|---|---|---|
| **US1** | Lease Request Submission | **DONE** | `feature/lease-request-submission` | PR #1 (`4c48967`, `d5ac96c`, `a563b30`) |
| **US2** | Multi-Document Upload & Validation | **DONE** | `feature/lease-request-submission` | PR #1 (`4c48967`, `f1159a7`, `a563b30`) |
| **US3** | Reusable Validation Service | **DONE** | `feature/status-tracking` | PR #4 (`0e31618`, `857fe68`) |
| **US4** | Inline Validation Feedback UI | **DONE** | `feature/status-tracking` | PR #4 (`0e31618`, `4b56754`, `857fe68`) |
| **US5** | Reviewer Queue Page | **DONE** | `feature/reviewer-workflow` | PR #3 (`088dadb`, `2a32a15`) |
| **US6** | Approve / Reject Decision | **DONE** | `feature/reviewer-workflow` | PR #3 (`18ecdde`, `e890a4e`, `2a32a15`) |
| **US7** | Request Changes & Resubmission | **DONE** | `feature/reviewer-workflow`, `feature/status-tracking` | PR #3 (`e890a4e`), PR #4 (`4b56754`, `857fe68`) |
| **US8** | Request Status Tracking View | **DONE** | `feature/status-tracking` | PR #4 (`4b56754`, `857fe68`) |
| **US9** | Append-Only Status Audit Trail | **DONE** | `feature/reviewer-workflow` | PR #3 (`18ecdde`, `e890a4e`, `857fe68`) |

---

## Story Acceptance Criteria Status

- [x] **US1**: Requester details & mandatory documents -> status `SUBMITTED` with generated Request ID.
- [x] **US2**: Supports PDF, JPG, PNG up to 5 MB per file; displays name and size before submit.
- [x] **US3**: Reusable `ValidationService` records `VALID` or `INVALID` + reason on each `Document`.
- [x] **US4**: Inline validation error rendering without persisting invalid requests.
- [x] **US5**: Reviewer Queue listing `SUBMITTED` / `UNDER_REVIEW` requests with document links.
- [x] **US6**: `POST /api/requests/{id}/approve` & `POST /api/requests/{id}/reject` (mandatory comment on reject). Persists `ReviewAction` and updates `status`.
- [x] **US7**: `POST /api/requests/{id}/request-changes` sets `CHANGES_REQUESTED`. Requester can resubmit back to `SUBMITTED`.
- [x] **US8**: Track My Request UI displaying status badge, timestamp, documents, and resubmit action.
- [x] **US9**: Append-only `StatusHistory` table tracking every status transition (`submit`, `approve`, `reject`, `request-changes`, `resubmit`). Chronological log exposed via `GET /api/requests/{id}/history`.
