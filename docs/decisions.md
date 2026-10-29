# Project Decision Register

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Last updated:** 2026-10-29  
**Status:** Working register of accepted decisions; implementation status is tracked separately.

## Purpose and authority

This register captures the decisions agreed during project planning and development so that later implementation remains consistent.

The professor's official assignment takes precedence if any entry conflicts with a mandatory requirement. This register is a project management aid, not a substitute for the official assignment or an implementation checklist.

An **accepted** decision means the team has chosen an approach, not that it has necessarily been implemented or tested. Changes must be explicitly discussed and recorded here and, for major architectural changes, in an Architecture Decision Record (ADR).

## 1. Domain and user experience

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-001 | Model a public library rather than a commercial lending service. | Planned domain. |
| D-002 | Use a hybrid physical-lending workflow: librarians perform checkout and return; members can browse, reserve and renew online. | Planned. |
| D-003 | Provide a member dashboard showing active and previous loans, reservations, fine information and purchase suggestions. | Planned; screen details to be designed in Figma. |
| D-004 | Provide three roles: `MEMBER`, `LIBRARIAN` and `ADMIN`, with genuinely different permissions. A staff user can also borrow when granted `MEMBER` privileges and is subject to ordinary borrowing rules. | Planned; final authorization matrix to be documented before security implementation. |
| D-005 | Track the issuing librarian and, when a loan is returned, the returning librarian. | Represented in ER diagram; not implemented. |

## 2. Borrowing, renewals and fines

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-006 | Standard loan period: **21 days**. | Planned. |
| D-007 | Maximum **five simultaneously active loans** per member. | Planned. |
| D-008 | A renewal extends the current due date by **14 days**; maximum **two renewals** per loan. | Planned. |
| D-009 | Do not renew overdue loans or loans whose title has an active reservation by another member. | Planned; aligns with P03 advanced renewal requirement. |
| D-010 | Fine: **€0.20 per loan per overdue calendar date** in the `Europe/Zagreb` time zone; a book is due through the end of its due date. Returning on an overdue date incurs that date's charge. | Planned; see ADR-001 for precise accrual/reconciliation policy. |
| D-011 | Outstanding unpaid fines do **not independently** block new borrowing. Other borrowing restrictions still apply. | Planned. |
| D-012 | Store fines as daily `FineAccrual` records, unique on (`LoanId`, `AccrualDate`). Nightly calculation and return processing reconcile missing charges without duplicating them. | Planned; idempotency must be proved with tests. |

## 3. Reservations and inventory

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-013 | Members can reserve a title **only when there are no borrowable copies** of that title. Reservations are title-level, not copy-level at creation. | Planned. |
| D-014 | Waiting-list order is determined by `RequestedAt`, then `Id` (FIFO with deterministic tie-break). | Planned. |
| D-015 | When a copy becomes available, automatically allocate it to the first eligible waiting reservation. | Planned; concurrency safeguards required. |
| D-016 | An allocated reservation has a **three-day pickup deadline**. Expired holds release the copy and promote the next eligible member. | Planned; precise timestamp handling to be tested. |
| D-017 | If an allocated copy is damaged or unavailable before pickup, the affected reservation keeps its original queue priority while allocation is reconsidered. | Planned. |
| D-018 | Keep `BookCopy.CopyCondition` separate from `BookCopy.Status`; status describes availability/workflow, condition describes physical quality. | ER diagram design; enum values still to be finalized. |
| D-019 | `BookTitle.Authors` is text; ISBN is optional. | ER diagram design. |
| D-020 | A fulfilled reservation can be linked to its resulting loan through nullable, unique `Loans.ReservationId`. | ER diagram design; database constraint not yet implemented. |

## 4. Persistence and integrity

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-021 | Use PostgreSQL with Spring Data JPA, Hibernate 7, Flyway and HikariCP. | PostgreSQL connection and framework startup verified; migrations and entities not implemented. See ADR-001. |
| D-022 | Use 12 logical application tables: `BookTitles`, `BookCopies`, `Categories`, `BookCategories`, `AppUsers`, `Roles`, `UserRoles`, `Loans`, `FineAccruals`, `Reservations`, `PurchaseSuggestions` and `AiSuggestionResults`. | ER diagram saved and committed; database tables not yet created. |
| D-023 | Use singular PascalCase Java entity classes, plural PascalCase logical SQL table names and PascalCase logical column names. Accept PostgreSQL lowercase folding for unquoted identifiers; map consistently in JPA and migrations. | Planned; physical naming strategy to be verified when migrations begin. |
| D-024 | Use `BookCategories` as an attributed association entity with `IsPrimary`; enforce at most one primary category per title. | ER diagram design; constraints pending. |
| D-025 | Preserve loans, fine accruals and important reservation history. Allow physical deletion where safe; use withdrawal/deactivation and reject deletion where protected history exists. | Planned; cascade rules to be determined per relationship. |
| D-026 | Use service-level transaction boundaries, database constraints and carefully planned locking for checkout, renewal, return and reservation allocation. | Architectural decision only; lock acquisition order and concurrency tests pending. |
| D-027 | Default to lazy JPA fetching where appropriate and apply fetch joins/entity graphs for specific use cases. Document an N+1 before/after SQL comparison. | Planned, required by LO4. |
| D-028 | Keep web controllers, service logic and repositories separate. Use constructor injection, DTOs/forms, validation and suitable exception handling. | Architecture policy; feature code not yet implemented. |

## 5. AI purchase suggestions and scheduling

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-029 | Members may submit free-text purchase suggestions. AI will propose subject categories and flag likely duplicate titles already held by the library. An acquisitions librarian makes the final decision. | P03 requirement; Google Gemini selected as provider. Feature not implemented. |
| D-030 | Retain multiple AI analysis results per purchase suggestion and keep AI output separate from the final human review record. | ER diagram design; database tables not yet implemented. |
| D-031 | Nightly fine accrual must be idempotent. Scheduling must also be safe when multiple application instances are running; select Quartz JDBC persistence or documented distributed locking. | Scheduler approach not yet selected; ADR-003 planned. |
| D-039 | Use **Google Gemini API** as the primary AI provider for the P03 purchase-suggestion feature. | Accepted during M1; API access and integration not yet verified. |
| D-040 | Start evaluation with **Gemini 2.5 Flash-Lite**, with **Gemini 2.5 Flash** as an alternative if necessary. The final model will depend on availability, quotas and evaluation results. | Provisional model selection; no live API evaluation performed. |
| D-041 | Supply the AI model with the member's suggestion, permitted subject categories and a shortlist of relevant existing catalog titles. Do not rely on the model to know the library's inventory independently. | Planned integration approach. Catalog retrieval and candidate selection not implemented. |
| D-042 | Require structured AI output containing suggested subject categories and possible duplicate candidates. Validate returned categories and title identifiers against application data before accepting the result. | Planned; final JSON schema and validation rules pending ADR-002. |
| D-043 | Use a provider-independent Java interface separating AI integration from business logic, with a deterministic stub implementation for automated tests. | Planned; interface and stub not implemented. |
| D-044 | AI output is advisory. The acquisitions librarian must review the results and record the final decision. AI must not independently approve purchases or modify catalog records. | Accepted workflow; not implemented. |
| D-045 | Keep AI credentials outside version control, minimize personal data sent to the provider, cap requests and handle timeouts, quota exhaustion and invalid responses. | Architectural policy; actual configuration and safeguards not implemented. |
| D-046 | Evaluate the real AI integration using at least 10 documented cases, including English and Croatian suggestions, exact and approximate duplicates, ambiguous inputs and suggestions without matches. | Planned; actual results will be recorded in `docs/ai-evaluation.md`. |

### AI provider decision rationale

Google Gemini was selected because the P03 AI task is relatively lightweight and structured.

The application needs to classify member purchase suggestions into predefined subject categories and identify possible duplicates among relevant existing catalog titles. It does not require training a custom model, maintaining a vector database or building an autonomous agent.

Gemini was considered suitable because it supports text analysis and structured responses. Eligible free-tier API access may also reduce the cost of development and evaluation.

The provider choice does not guarantee that every model will be free, available or sufficiently accurate. These conditions must be verified against the actual API account and model behavior during implementation.

The planned approach is:

1. A member submits a title and free-text purchase suggestion.
2. The application stores the suggestion and retrieves relevant catalog candidates.
3. The AI adapter sends the suggestion, valid categories and candidate titles to Gemini.
4. The application validates the structured result and stores the analysis.
5. An acquisitions librarian reviews the proposed categories and duplicate flags.
6. The librarian records the final decision independently of the AI output.

The application must support the required AI feature through both the Thymeleaf interface and a REST endpoint.

A provider-independent interface and deterministic testing stub will allow automated tests without live API calls. Actual evaluation of the real provider will be documented separately.

**ADR-002: AI Integration and Human Review** will contain the detailed technical design, alternatives, response schema, error handling and verification strategy.

## 6. Technology, repository and workflow

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-032 | Use Java 25, Spring Boot 4.1, Maven Wrapper and IntelliJ IDEA. | Java 25.0.5, IntelliJ IDEA 2026.2.3, Spring Boot 4.1.2 and Maven Wrapper 3.10.0 verified locally. |
| D-033 | Use PostgreSQL 17 in Docker Compose for local development, with host-only binding on `127.0.0.1:5433` and a dedicated persistent named volume. | Docker container was observed healthy and SQL connection verified (2026-10-29). |
| D-034 | Manage local credentials through an ignored `.env` file for Docker and a local IntelliJ run configuration for Spring Boot; never commit actual database passwords. The IDE configuration may store a locally readable password and must not be shared. Commit `.env.example` with placeholders only. | Implemented in initial setup. An actual password was previously committed to `.env.example`; the Git history was rewritten and force-pushed to replace it with a placeholder. The old password was not rotated and must still be considered potentially exposed. |
| D-035 | Use GitHub repository `LeoBrcina/library-lending-system` and small, descriptive commits without conventional prefixes. | Seven logical commits preserved after the history rewrite; `main` synchronized with `origin/main` as last verified on 2026-10-29. |
| D-036 | Use three ADRs for major decisions and maintain `docs/ai-usage.md`, `docs/ai-evaluation.md` and Figma documentation as the project progresses. | ADR-001, the decision register and AI usage log committed; remaining documentation planned. |
| D-037 | Use the professor's assignment and uploaded lectures/demos as primary references. For external GitHub source-code reference, use only the two professor-authorized repositories. | Ongoing policy. |
| D-038 | Do not implement major phases simultaneously; verify before moving forward, keep code paste-ready and review current files before modifying them. | Ongoing workflow policy. |
| D-047 | Deploy the Spring Boot application on **Render Free** from a Dockerfile, with GitHub integration and public HTTPS access. | Accepted M1 hosting choice; no deployment performed. Free instances may sleep when idle. |
| D-048 | Use **Neon Free** for managed PostgreSQL in production, keeping PostgreSQL 17 through Docker Compose for local development. Require TLS and environment-supplied production credentials. | Accepted M1 database-hosting choice; no Neon database created or connection tested. |
| D-049 | Keep deployment spending at **€0**, without entering a payment card or enabling paid services. Verify free-tier conditions and resource limits before creating accounts. | Accepted project constraint; availability and quotas must be rechecked during deployment. |
| D-050 | Use configurable scheduling: normal calendar-based execution and a short demonstration interval (for example, five seconds). Scheduling frequency must never change the per-calendar-day fine calculation. | Planned; job configuration and tests not implemented. |
| D-051 | Treat Render Free sleep as an availability limitation: overdue fine processing must reconcile missed dates when execution resumes, with database-backed idempotency and coordination for multiple instances. A five-second demonstration does not prove unattended nightly execution. | Accepted design constraint; final scheduler mechanism and reliability tests pending ADR-003. |

### Hosting decision rationale

Render Free and Neon Free were selected because the professor explicitly recommends this pair as a no-payment-card route to the course's free public deployment requirement. Render can host the Dockerized Spring Boot application behind HTTPS, while Neon supplies a separate managed PostgreSQL database. The target cost is €0, and no paid upgrade is authorized.

The production setup will use a dedicated Spring profile, environment variables for database and Gemini credentials, Flyway migrations, an Actuator health check and a documented public HTTPS URL. Docker build and runtime behavior must be verified locally before deployment. These are planned implementation steps, not completed work.

Render Free may sleep when idle, so JVM-based scheduled tasks cannot be assumed to run at their intended times. We will preserve calendar-based fine calculations, make the job idempotent and reconcile missed dates after resumption. A configurable five-second schedule can demonstrate repeated execution and the absence of duplicate charges during the defense, but does not establish guaranteed overnight execution. The persistence/locking strategy remains to be finalized in ADR-003.

## 7. Additional course obligations

The following requirements are tracked to prevent omission. Some architectural decisions have been accepted, but the corresponding implementation has not yet been completed.

| Requirement | Current position |
|---|---|
| Spring Security 7, BCrypt, CSRF, three roles and service-level authorization | Required for LO2; permissions matrix pending. |
| REST API with DTOs, pagination, sorting, status codes and ProblemDetail | Required for LO5; endpoint design pending. |
| AI feature accessible from both a screen and an endpoint | Required for LO5; Google Gemini selected, implementation pending. |
| Provider-independent AI integration and deterministic stub tests | Approach accepted; implementation pending. |
| JUnit 6, Mockito, MVC/repository/Testcontainers tests and JaCoCo service coverage | Required for LO6; no passing test result or coverage evidence yet. |
| Croatian/English internationalization, including validation, date and number formatting | Required for LO7; not started. |
| Two scheduled jobs and persistent execution/locking strategy | Required for LO7; not started. |
| Figma low-/high-fidelity prototype, components, interactive flow and responsive views | Required prototype deliverables; not started. |
| AI evaluation document with at least 10 actual cases | Required final documentation; evaluation not started. |
| Managed database, HTTPS deployment, CI, performance evidence and recording | Render Free + Neon Free selected; actual deployment and evidence not yet verified. |

## 8. Implementation checkpoints and unresolved decisions

### Verified M1 technical baseline (2026-10-29)

- Git repository created with seven logical commits on `main`, synchronized with `origin/main` after the history rewrite.
- Java 25.0.5 and Maven Wrapper 3.10.0 verified.
- Spring Boot 4.1.2 built successfully with tests deliberately skipped.
- PostgreSQL 17.11 reported healthy through Docker Compose.
- `psql` returned database `library_lending` and user `library_app`.
- Spring Boot connected successfully through HikariCP and initialized Flyway and Hibernate.
- Application started through Git Bash and IntelliJ IDEA.
- HTTP request to `/` returned an expected 404 because no controller exists.
- The Git history containing the Docker Compose commit was rewritten to replace the exposed `.env.example` password with a placeholder, and the new remote branch was verified.

### Accepted decisions not yet implemented

- Google Gemini is the selected AI provider.
- Gemini 2.5 Flash-Lite is the provisional initial model; Gemini 2.5 Flash is an alternative.
- Structured AI classification, catalog duplicate suggestions, provider-independent integration and librarian confirmation are planned.
- No real AI API calls, model evaluation results or AI integration tests have been verified.
- Render Free is the selected web host and Neon Free the selected managed PostgreSQL service; no deployment has been performed.
- A configurable five-second defense demonstration is planned for scheduled jobs without modifying calendar-day fine rules.
- Free-instance sleep and reliable unattended scheduling remain unresolved deployment risks.

### Still to settle or complete

- Professor's explicit topic acceptance has not been confirmed in the conversation.
- Record the accepted hosting selection and verify account-level free-tier conditions when deployment begins.
- Design and retain Figma low-fidelity wireframes.
- Finalize the security permission matrix, entity enum values and transaction locking strategy before implementation.
- Create migrations, seed data and application functionality in subsequent milestones.
- Create ADR-002 for detailed AI integration design and ADR-003 for persistent scheduling.
- Implement and evaluate the actual AI integration, recording at least 10 genuine cases.
- Keep `docs/ai-usage.md` current and complete the remaining required documentation.

## 9. Change management

For each new or revised decision:

1. Add or update the relevant decision ID and describe the reason.
2. Note whether the change affects the ER diagram, ADRs, implementation, tests or Figma.
3. Verify the resulting behavior where implementation exists.
4. Make a focused Git commit after review.

Do not present planned items as completed or infer test success from successful compilation.