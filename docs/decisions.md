# Project Decision Register

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Last updated:** 2026-10-29  
**Status:** Working register of accepted decisions; implementation status is tracked separately.

## Purpose and authority

This register captures the decisions agreed during project planning and M1 setup so that later implementation remains consistent. The professor's official assignment takes precedence if any entry conflicts with a mandatory requirement. This register is a project management aid, not a substitute for the official assignment or an implementation checklist. An **accepted** decision means the team has chosen an approach, not that it has been implemented or tested. Changes must be explicitly discussed and recorded here and, for major architectural changes, in an Architecture Decision Record (ADR).

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

## 5. AI purchases and scheduling

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-029 | Members may submit free-text purchase suggestions. AI will propose subject categories and flag likely duplicate titles already held by the library. An acquisitions librarian makes the final decision. | P03 requirement; provider and implementation not yet selected. |
| D-030 | Retain multiple AI evaluation results per purchase suggestion and keep AI output separate from the final human review record. | ER diagram design. |
| D-031 | Nightly fine accrual must be idempotent. Scheduling must also be safe when multiple application instances are running; select Quartz JDBC persistence or documented distributed locking. | Scheduler approach not yet selected; ADR-003 planned. |

## 6. Technology, repository and workflow

| ID | Accepted decision | Implementation status / notes |
|---|---|---|
| D-032 | Use Java 25, Spring Boot 4.1, Maven Wrapper and IntelliJ IDEA. | Java 25.0.5, IntelliJ IDEA 2026.2.3, Spring Boot 4.1.2 and Maven Wrapper 3.10.0 verified locally. |
| D-033 | Use PostgreSQL 17 in Docker Compose for local development, with host-only binding on `127.0.0.1:5433` and a dedicated persistent named volume. | Docker container was observed healthy and SQL connection verified (2026-10-29). |
| D-034 | Manage local credentials through an ignored `.env` file for Docker and a local IntelliJ run configuration for Spring Boot; never commit actual database passwords. The IDE configuration may store a locally readable password and must not be shared. Commit `.env.example`. | Implemented in initial setup; repository history review for all secrets remains an ongoing responsibility. |
| D-035 | Use GitHub repository `LeoBrcina/library-lending-system` and small, descriptive commits without conventional prefixes. | Five commits pushed and `main` synced as of 2026-10-29. |
| D-036 | Use three ADRs for major decisions and maintain `docs/ai-usage.md`, `docs/ai-evaluation.md` and Figma documentation as the project progresses. | ADR-001 committed; decision register and AI usage log drafted for review; other documentation planned. |
| D-037 | Use the professor's assignment and uploaded lectures/demos as primary references. For external GitHub source-code reference, use only the two professor-authorized repositories. | Ongoing policy. |
| D-038 | Do not implement major phases simultaneously; verify before moving forward, keep code paste-ready and review current files before modifying them. | Ongoing workflow policy. |

## 7. Additional course obligations (not yet implementation decisions)

The following items are required by the professor and are tracked here to prevent omission; their implementation details have **not** been agreed or verified:

| Requirement | Current position |
|---|---|
| Spring Security 7, BCrypt, CSRF, three roles and service-level authorization | Required for LO2; permissions matrix pending. |
| REST API with DTOs, pagination, sorting, status codes and ProblemDetail | Required for LO5; endpoint design pending. |
| AI feature accessible from both a screen and an endpoint | Required for LO5; provider and adapter pending. |
| JUnit 6, Mockito, MVC/repository/Testcontainers tests and JaCoCo service coverage | Required for LO6; no passing test result or coverage evidence yet. |
| Croatian/English internationalization, including validation, date and number formatting | Required for LO7; not started. |
| Two scheduled jobs and persistent execution/locking strategy | Required for LO7; not started. |
| Figma low-/high-fidelity prototype, components, interactive flow and responsive views | Required prototype deliverables; not started. |
| AI evaluation document with at least 10 actual cases | Required final documentation; not started. |
| Managed database, HTTPS deployment, CI, performance evidence and recording | Required later deliverables; not yet verified. |

## 8. Implementation checkpoints and unresolved decisions

**Verified M1 technical baseline (2026-10-29):**

- Git repository created with five commits pushed to `origin/main`.
- Java 25.0.5 and Maven Wrapper 3.10.0 verified.
- Spring Boot 4.1.2 built successfully with tests deliberately skipped.
- PostgreSQL 17.11 reported healthy through Docker Compose.
- `psql` returned database `library_lending` and user `library_app`.
- Spring Boot connected successfully through HikariCP and initialized Flyway and Hibernate.
- Application started through Git Bash and IntelliJ IDEA.
- HTTP request to `/` returned an expected 404 because no controller exists.

**Still to settle or complete:**

- Professor's explicit topic acceptance has not been confirmed in the conversation.
- Select and record the AI provider and hosting platform for M1.
- Design and retain Figma low-fidelity wireframes.
- Finalize security permissions, entity enum values and transaction locking strategy before implementation.
- Create migrations, seed data and all application functionality in subsequent milestones.
- Keep `docs/ai-usage.md` current and create the remaining required AI and architecture documentation.

**Working tree note:** The latest reported local `git status` showed an unstaged Markdown formatting change to `docs/adr/001-persistence-model.md`. It was not part of the datasource configuration commit.

## 9. Change management

For each new or revised decision:

1. Add or update the relevant decision ID and describe the reason.
2. Note whether the change affects the ER diagram, ADRs, implementation, tests or Figma.
3. Verify the resulting behavior where implementation exists.
4. Make a focused Git commit after review.

Do not present planned items as completed or infer test success from a successful compilation.