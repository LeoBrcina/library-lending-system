# Project Decision Register

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Last updated:** 2026-11-13

The professor's official assignment takes precedence over project decisions. An accepted decision does not necessarily mean that the corresponding feature has been implemented.

## 1. Domain and user experience

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-001 | Model a public library rather than a commercial lending service. | Accepted domain. |
| D-002 | Use a hybrid physical-lending workflow: librarians perform checkout and return; members can browse, reserve and renew online. | Planned. |
| D-003 | Provide a member dashboard showing active and previous loans, reservations, fine information and purchase suggestions. | M1 wireframe prepared; implementation planned. |
| D-004 | Provide three roles: `MEMBER`, `LIBRARIAN` and `ADMIN`, with different permissions. Staff may also borrow when assigned the `MEMBER` role. | All three roles seeded in V3; security implementation pending. |
| D-005 | Track the issuing librarian and, when a loan is returned, the returning librarian. | Foreign keys created in V1. V2 requires return timestamp and returning staff to be recorded together. |

## 2. Borrowing, renewals and fines

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-006 | Standard loan period: **21 days**. | Service implementation planned. |
| D-007 | Maximum **five simultaneously active loans** per member. | Service implementation planned. |
| D-008 | A renewal extends the current due date by **14 days**; maximum **two renewals** per loan. | V1 constrains renewal count to 0–2. Renewal logic pending. |
| D-009 | Do not renew overdue loans or loans whose title has an active reservation by another member. | Service implementation planned. |
| D-010 | Fine: **€0.20 per loan per overdue calendar date** in `Europe/Zagreb`; a book is due through the end of its due date. Returning on an overdue date incurs that date's charge. | Fine calculation and reconciliation pending. |
| D-011 | Outstanding unpaid fines do **not independently** block new borrowing. | Service implementation planned. |
| D-012 | Store fines as daily `FineAccrual` records, unique on (`LoanId`, `AccrualDate`). Nightly calculation and return processing reconcile missing charges without duplicates. | Table and unique constraint implemented in V1. Scheduling and reconciliation pending. |

## 3. Reservations and inventory

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-013 | Members can reserve a title **only when no borrowable copies are available**. Reservations are created at title level. | Service implementation planned. |
| D-014 | Waiting-list order uses `RequestedAt`, then `Id`, for deterministic FIFO ordering. | Supporting index created in V1; allocation algorithm pending. |
| D-015 | Automatically allocate an available copy to the first eligible waiting reservation. | Transactional service implementation planned. |
| D-016 | Allocated reservations have a **three-day pickup deadline**. Expired holds release the copy and promote the next eligible member. | Deadline and expiration logic pending. |
| D-017 | If an allocated copy becomes damaged or unavailable before pickup, the reservation retains its original queue priority while allocation is reconsidered. | Service implementation planned. |
| D-018 | Keep `BookCopy.CopyCondition` separate from `BookCopy.Status`. Condition represents physical quality with the accepted values `NEW`, `GOOD`, `FAIR`, `POOR` and `DAMAGED`. Status represents circulation state with the accepted values `AVAILABLE`, `ON_LOAN`, `ON_HOLD`, `MAINTENANCE` and `WITHDRAWN`. | Separate columns implemented in V1. Java enums and entity-level validation implemented. Creating or updating a copy to `POOR` or `DAMAGED` places it in `MAINTENANCE`. String-based JPA mappings retained. Database allowed-value constraints and full circulation transitions pending. |
| D-019 | Store `BookTitle.Authors` as text and allow ISBN to be optional. | Implemented in V1. |
| D-020 | Link fulfilled reservations to loans using nullable, unique `Loans.ReservationId`. | Foreign key and unique constraint implemented in V1. Fulfillment logic pending. |
| D-054 | Separate physical-copy administration from circulation. Allow metadata changes only for available or maintenance copies. Preserve loan and reservation history during withdrawal or deletion. Maintenance release requires an active title, acceptable physical condition and no conflicting loan, hold or waiting reservation. | Service operations, integration tests and librarian-facing Add, Edit, Withdraw, Delete and Release workflows implemented and verified in M2 Steps 19.1 and 19.2. Withdrawal retains the copy record; deletion is refused when history exists. |
| D-055 | Acquisition dates are optional but cannot be in the future. Keep this validation at the form and service layers. | Implemented for physical-copy creation and editing. Future dates are rejected; missing dates remain valid. |
| D-056 | Require confirmation before withdrawal and permanent deletion. Use POST for state changes, show meaningful errors and keep service-layer rules authoritative. | Separate Thymeleaf confirmation pages and MVC endpoints implemented and tested. Release uses POST. |
| D-057 | Use the existing responsive inventory table for physical copies, with horizontal scrolling on narrow screens. | Implemented and manually checked on mobile. A separate physical-copy card view was considered but not adopted. |

## 4. Persistence and integrity

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-021 | Use PostgreSQL with Spring Data JPA, Hibernate 7, Flyway and HikariCP. | PostgreSQL 17.11 and Flyway V1–V3 verified. Catalog JPA entities, repositories and services implemented. Remaining domain mappings pending. See ADR-001. |
| D-022 | Use 12 application tables: `BookTitles`, `BookCopies`, `Categories`, `BookCategories`, `AppUsers`, `Roles`, `UserRoles`, `Loans`, `FineAccruals`, `Reservations`, `PurchaseSuggestions` and `AiSuggestionResults`. | All 12 tables created by V1 and verified in PostgreSQL. |
| D-023 | Use singular PascalCase Java entities, plural PascalCase logical SQL table names and PascalCase logical column names. PostgreSQL lowercase folding is accepted; JPA mappings must match. | SQL naming and catalog JPA mappings verified. Remaining mappings pending. |
| D-024 | Model `BookCategories` as an attributed association entity with `IsPrimary`. Allow at most one primary category per title. | Composite primary key and partial unique index implemented in V1. JPA mapping and repository tests implemented. |
| D-025 | Preserve loans, fine accruals and important reservation history. Allow physical deletion where safe; use withdrawal or deactivation when history must be retained. | Restrictive foreign keys implemented in V1–V2. Book Copy deletion and withdrawal checks implemented and tested at service level and exposed through the librarian interface. Other historical deletion rules remain pending. |
| D-026 | Use service-level transactions, database constraints and suitable locking for checkout, renewal, return and reservation allocation. | Selected constraints implemented in V1–V2. Book Copy administrative operations use pessimistic row locking. Circulation transactions and concurrency tests pending. |
| D-027 | Prefer lazy JPA fetching and use fetch joins or entity graphs when needed. Document an N+1 problem and its before/after SQL. | Lazy catalog mappings implemented. N+1 measurement and optimization evidence pending. |
| D-028 | Separate controllers, services and repositories. Use constructor injection, DTOs/forms, validation and appropriate exception handling. | Applied to Book Title and Physical Copy management, including the web forms, service DTOs and administrative actions. Remaining application features pending. |
| D-052 | Keep applied Flyway versioned migrations unchanged. Use new migrations for later schema modifications. | Applied during V1–V3 development. |
| D-053 | Separate permanent reference data from development-only demonstration data. Do not include default user credentials or date-dependent demonstration records in production migrations. | V3 contains three roles and ten categories. Separate development-only catalog fixtures implemented. |

## 5. AI purchase suggestions and scheduling

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-029 | Members submit free-text purchase suggestions. AI proposes categories and possible duplicate titles; an acquisitions librarian makes the final decision. | Google Gemini selected; feature pending. |
| D-030 | Retain multiple AI analysis results for a purchase suggestion and keep AI output separate from the librarian's decision. | `PurchaseSuggestions` and `AiSuggestionResults` tables implemented in V1. Workflow pending. |
| D-031 | Nightly fine accrual must be idempotent and safe across multiple application instances. Use Quartz JDBC persistence or documented distributed locking. | Scheduler design pending ADR-003. |
| D-039 | Use **Google Gemini API** as the primary AI provider. | Accepted; integration not implemented. |
| D-040 | Start evaluation with **Gemini 2.5 Flash-Lite**, with **Gemini 2.5 Flash** as a possible alternative. | Provisional choice; live availability and behavior not verified. |
| D-041 | Supply Gemini with the member's suggestion, permitted categories and relevant existing catalog titles. Do not assume the model knows the library inventory. | Planned. |
| D-042 | Require structured AI output containing category suggestions and possible duplicate candidates. Validate category and title identifiers before accepting results. | Planned; response schema pending ADR-002. |
| D-043 | Separate AI integration from business logic through a provider-independent Java interface and a deterministic test stub. | Planned. |
| D-044 | AI output is advisory. A librarian must confirm the final decision; AI must not independently approve purchases or change catalog records. | Planned. |
| D-045 | Keep AI credentials outside Git, limit personal data, control request volume and handle timeouts, quotas and invalid responses. | Planned. |
| D-046 | Evaluate the live AI integration with at least ten documented cases, including English/Croatian suggestions, duplicates, ambiguous inputs and unmatched titles. | Evaluation pending in `docs/ai-evaluation.md`. |

### AI provider rationale

Google Gemini was selected for the purchase-suggestion feature because the task involves text classification and comparison against a limited set of catalog records.

The application will provide valid library categories and relevant existing titles to the model. The result will be checked against application data before storage, and a librarian will make the final decision.

A custom-trained model or vector database is not required for the initial scope. Exact model availability, free-tier limits and classification quality will be checked during implementation.

The planned workflow is:

1. A member submits a title and description.
2. The application stores the suggestion and retrieves relevant catalog candidates.
3. The AI adapter sends the permitted categories and candidate titles with the suggestion.
4. The response is validated and saved.
5. A librarian reviews the suggested categories and duplicate candidates.
6. The librarian records the decision independently.

The AI feature must be accessible through both a Thymeleaf screen and a REST endpoint.

ADR-002 will document the provider interface, structured output, error handling and verification approach.

## 6. Technology, repository and workflow

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-032 | Use Java 25, Spring Boot 4.1, Maven Wrapper and IntelliJ IDEA. | Java 25.0.5, Spring Boot 4.1.2, Maven Wrapper 3.10.0 and IntelliJ IDEA 2026.2.3 verified. |
| D-033 | Run PostgreSQL 17 through Docker Compose locally, bound to `127.0.0.1:5433`, with a persistent named volume. | PostgreSQL 17.11 and V1–V3 migrations verified. |
| D-034 | Use ignored `.env` files and local IntelliJ environment settings for credentials. Commit only placeholders in `.env.example`. | Applied locally. |
| D-035 | Use `LeoBrcina/library-lending-system` and small descriptive commits without conventional prefixes. | Repository active. Book Title management, Book Copy service and physical-copy inventory workflows through withdrawal committed and pushed to `main`; CI passed as reported by the developer. Physical-copy deletion is locally verified and pending commit. |
| D-036 | Maintain three ADRs, `docs/ai-usage.md`, `docs/ai-evaluation.md` and Figma documentation. | ADR-001 and initial documentation present; ADR-002, ADR-003 and evaluation document pending. |
| D-037 | Use the professor's assignment and uploaded lectures/demos as primary references. Only the two authorized professor GitHub repositories may be used as external code references. | Ongoing policy. |
| D-038 | Implement one major phase at a time, inspect current files before changing them and verify work before committing. | Ongoing workflow policy. |
| D-047 | Deploy Spring Boot to **Render Free** through Docker with GitHub integration and HTTPS. | Selected; deployment pending. |
| D-048 | Use **Neon Free** for production PostgreSQL with TLS and environment-supplied credentials. | Selected; setup pending. |
| D-049 | Keep deployment spending at **€0** with no payment card or paid service. | Accepted; actual free-tier limits to be checked before deployment. |
| D-050 | Support normal calendar-based scheduling and a short demonstration interval. Scheduling frequency must not change daily fine calculations. | Planned. |
| D-051 | Account for Render Free sleep by reconciling missed fine dates and coordinating execution across instances. A short demonstration interval does not prove overnight reliability. | Planned; ADR-003 pending. |

### Hosting rationale

Render Free and Neon Free were chosen as the intended no-cost deployment combination for the course project.

The application will be packaged in Docker, deployed through GitHub and connected to Neon PostgreSQL using environment-based credentials and TLS. Production setup will also require health checks and a public HTTPS endpoint.

Render Free instances may sleep when inactive, so scheduled fine processing cannot depend solely on the application remaining awake. The job must reconcile missed dates after resuming, avoid duplicate charges and coordinate execution safely.

A short interval can demonstrate repeated scheduling during the defense, but it does not establish reliable unattended overnight execution.

## 7. Remaining course requirements

| Requirement | Current position |
|---|---|
| Spring Security 7, BCrypt, CSRF, three roles and service-level authorization | Role reference data implemented; authentication and authorization pending. |
| REST API with DTOs, pagination, sorting, status codes and ProblemDetail | Planned for LO5. |
| AI feature through screen and endpoint | Google Gemini selected; implementation pending. |
| AI provider abstraction and deterministic stub tests | Planned. |
| JUnit 6, Mockito, MVC/repository/Testcontainers tests and JaCoCo coverage | JUnit, MVC, repository and service integration tests implemented. Testcontainers and JaCoCo coverage work pending. |
| Croatian/English internationalization, validation, date and number formatting | Planned for LO7. |
| Two scheduled jobs and persistent coordination | Planned for LO7. |
| Figma prototypes and reusable components | M1 wireframes and M2 high-fidelity prototype prepared. |
| AI evaluation with at least ten real cases | Pending. |
| Managed database, HTTPS deployment, CI, performance measurements and recording | CI implemented; deployment and performance evidence pending. |

## 8. Implementation checkpoints

### M1 technical baseline — 2026-10-29

- Git repository initialized and synchronized with GitHub.
- Java 25.0.5 and Maven Wrapper 3.10.0 verified.
- Spring Boot 4.1.2 package build succeeded with tests skipped.
- PostgreSQL 17.11 started in Docker Compose.
- Spring Boot connected through HikariCP and initialized Flyway and Hibernate.
- Tomcat started on port 8080.
- HTTP request to `/` returned the expected 404 before controllers were added.
- Git history was rewritten after a password was found in `.env.example`. The password was not rotated.

### M2 database foundation — 2026-11-05

- V1 created all 12 application tables.
- PostgreSQL inspection confirmed the expected table count and selected uniqueness indexes.
- V2 added reservation status and lifecycle checks, active-reservation indexes, copy/title consistency and loan-return constraints.
- PostgreSQL inspection confirmed the V2 constraints.
- The developer reported six negative SQL tests passing. Individual test-output logs were not supplied.
- V3 added three user roles and ten library categories, verified through SQL queries.
- All three Flyway migration files were committed to GitHub.
- The original manually prepared M1 ER diagram was retained unchanged.
- JPA entities, repositories, services, CRUD screens and development fixtures had not yet been implemented at this checkpoint.

### Accepted but not implemented

- Google Gemini integration and model evaluation.
- Render and Neon deployment.
- Security and permission matrix.
- Transactional circulation and reservation workflows.
- Persistent scheduled jobs and catch-up processing.

### M2 catalog management and physical-copy validation — 2026-11-12

- Catalog persistence, service operations and Thymeleaf management screens were implemented incrementally after the M2 database foundation.
- Book Title creation, details, editing, validation, category selection and catalog navigation were implemented and manually verified.
- MVC tests were added for successful submissions, invalid input, book details, edit-form prepopulation and missing book titles.
- The developer reported 42 automated tests passing after completing the Book Title management workflow.
- The Book Title details and editing implementation was committed and pushed as `9990882` (`implement and test book title details and editing`). The commit was inspected on GitHub.
- M2 Step 19.1A introduced `BookCopyCondition` and `BookCopyStatus` with five accepted values each.
- `BookCopy` validates inventory codes, physical conditions and circulation statuses through its constructor and setters.
- Inventory codes are trimmed and checked for required content and maximum length. Invalid condition and status values are rejected.
- The existing string-based JPA fields and PostgreSQL columns were retained for compatibility with the working catalog implementation.
- Nine focused `BookCopyValidationTests` tests were added.
- The developer reported all 51 tests passing with `./mvnw verify`, including the nine new validation tests.
- No Flyway migration was introduced in Step 19.1A. Reservation statuses remain constrained by the existing V2 migration.
- Java validation does not protect direct SQL writes or enforce full circulation state transitions.

### M2 physical-copy service operations — 2026-11-12

- Steps 19.1B–19.1E introduced the `BookCopyService`, DTOs, business exceptions and PostgreSQL integration tests.
- Creation requires an active parent title, valid physical condition and globally unique inventory code.
- Copies are retrieved by parent Book Title and returned as DTOs.
- Metadata updates allow changes to the inventory code, condition and acquisition date while preserving the parent relationship.
- Editing is restricted to `AVAILABLE` and `MAINTENANCE` copies. Other circulation states cannot be changed through ordinary inventory editing.
- Withdrawal retains the physical-copy record and prevents further circulation. Active loans and allocated reservations block withdrawal.
- Physical deletion is allowed only when no loan or reservation history references the copy. PostgreSQL foreign keys provide additional protection.
- Administrative update, withdrawal, deletion and maintenance release use pessimistic locking on the physical-copy record.
- A repaired copy may be released from maintenance only if its title is active, its condition is `NEW`, `GOOD` or `FAIR`, and there is no active loan, allocated reservation or waiting reservation for the title.
- Seven create/list integration tests, eight update tests, ten lifecycle tests and nine maintenance-release tests were added.
- The developer reported 58 passing tests after Step 19.1B, 66 after 19.1C, 76 after 19.1D and 85 after 19.1E.
- The `POOR` creation/update condition handling was corrected and regression-tested. `POOR` and `DAMAGED` copies enter maintenance.
- No new Flyway migrations were introduced.
- Database allowed-value constraints, complete circulation locking and concurrent duplicate-code handling remain future work.
- The service implementation was committed and pushed in `e3fc57a`; the developer reported that GitHub Actions passed.

### M2 physical-copy web management — 2026-11-12 to 2026-11-13

- Step 19.2A added a read-only physical-copy inventory table to Book Title Details, showing inventory codes, conditions, circulation statuses, acquisition dates and total/available counts.
- Step 19.2B added an Add Physical Copy form with validation, duplicate-code feedback and service-driven initial circulation status.
- Future acquisition dates are rejected in the form and service; acquisition dates remain optional.
- Step 19.2C added prepopulated Edit forms and title-scoped copy lookup. Editing is restricted to available or maintenance copies.
- Physical Copy Create and Edit validation summaries were refined so field-level errors are not repeated at the top of the page.
- Step 19.2D.1 added Release from Maintenance through POST. The service checks current condition, active title and conflicting circulation or waiting reservations.
- Step 19.2D.2 added Withdraw with a dedicated confirmation page. Withdrawn copies remain recorded but are not available for circulation.
- Step 19.2D.3 added Delete with a separate confirmation page. The service blocks deletion of copies with loan or reservation-allocation history.
- The Delete action uses red danger styling. The application retains the desktop inventory table with horizontal scrolling on mobile.
- The developer manually verified creation, editing, maintenance release, withdrawal, safe deletion, confirmation and cancellation, restricted-status actions, validation errors and responsive behavior.
- The developer reported the full Maven build passing after completing the deletion workflow and the final button styling correction.
- The developer pushed the preceding inventory-management checkpoint to GitHub and reported passing CI. The deletion changes and this documentation update remain pending commit.
- A read-only SQL join between physical copies and loans returned no rows in the local database, so rejection of deletion due to historical loans could not be demonstrated manually with the available data. Historical restrictions are covered by service integration tests; they were not established through that manual browser test.
- No new Flyway migration was required.

### Next development work

- Commit and push the verified physical-copy deletion changes and updated documentation.
- Confirm GitHub Actions passes for that commit.
- Review the completed M2 inventory deliverables against the official assignment.
- Implement the remaining circulation and reservation workflows, with deliberate transaction and locking rules.
- Prepare ADR-002 and ADR-003 during their respective implementation phases.
- Continue updating documentation after verified changes.

## 9. Change management

Record new decisions when they affect the architecture, business rules or implementation approach. Keep the corresponding documentation synchronized with the code.

Verify implemented behavior before marking a requirement complete. Use focused commits for reviewed changes.