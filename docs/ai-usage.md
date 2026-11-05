
# AI Usage Log

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Started:** 2026-10-29  
**Last updated:** 2026-11-05

## M1 — Planning and project setup

### AI-001 — Interpret P03 requirements and plan milestones

- **Date:** M1 planning phase (documented on 2026-10-29).
- **Task:** Review the professor's P03 requirements and plan the Spring Boot application and development milestones.
- **AI assistance:** Helped break down the required domain, persistence, security, web interface, REST API, AI integration, testing and deployment work into manageable steps.
- **Developer work:** Selected the public-library domain, hybrid librarian-managed checkout and return, member dashboard and incremental development workflow. The professor's assignment was established as the primary authority.
- **Changes and decisions:** Physical checkout and return were assigned to librarians rather than implementing unrestricted self-service borrowing. Work was divided into small steps with verification before committing.
- **Verification:** The planned scope and milestone sequence were reviewed. No separate professor sign-off was independently verified.
- **Files:** `docs/decisions.md`, `docs/adr/001-persistence-model.md`.

### AI-002 — Define business rules and ER diagram

- **Date:** M1 design phase (documented on 2026-10-29).
- **Task:** Design the library data model and borrowing rules.
- **AI assistance:** Suggested database relationships, fields and constraints for the 12-table model. Explained the book title/copy distinction, association entities, optional relationships, reservation allocation and daily fine records.
- **Developer work:** Chose the 21-day loan period, two 14-day renewals, five-loan limit, €0.20 daily fine, three-day reservation pickup deadline and policy that outstanding fines do not independently prevent borrowing. Created and arranged the ER diagram manually in draw.io.
- **Changes and decisions:** Issuing and returning librarians were recorded separately. The diagram was kept at a reasonable level of detail rather than adding unnecessary entities or polishing its appearance further.
- **Verification:** The ER diagram was reviewed using screenshots. The `.drawio` source and PDF export were committed. No database migration had been implemented at this stage.
- **Files:** `docs/diagrams/LibraryLendingSystem_ERD.drawio` and its PDF export.

### AI-003 — Write ADR-001

- **Date:** 2026-10-29.
- **Task:** Prepare the persistence architecture decision record.
- **AI assistance:** Drafted the ADR covering PostgreSQL, Spring Data JPA, Flyway, transaction boundaries, reservation handling, fine idempotency, deletion policies and fetching strategies.
- **Developer work:** Reviewed the draft, requested a second check against the project specification, accepted the corrected version and committed it.
- **Changes and decisions:** Clarified overdue date boundaries, database constraints, concurrency risks and the planned N+1 investigation.
- **Verification:** Commit `3d547a6` added the ADR. This confirmed the document was committed, not that the planned functionality was implemented.
- **Files:** `docs/adr/001-persistence-model.md`.

### AI-004 — Organize GitHub repository

- **Date:** 2026-10-29.
- **Task:** Establish the repository structure and initial Git history.
- **AI assistance:** Suggested documentation directories, `.gitignore` practices and small commits for individual changes.
- **Developer work:** Created `LeoBrcina/library-lending-system`, configured Git, connected the remote repository and pushed the initial commits.
- **Changes and decisions:** Chose descriptive commit messages without conventional prefixes. Documentation and implementation changes were committed separately.
- **Verification:** Git history and branch synchronization were inspected. The original logical commit sequence was preserved during a later history rewrite.
- **Files:** Git repository and project directory structure.

### AI-005 — Configure development environment

- **Date:** 2026-10-29.
- **Task:** Check Java, IntelliJ IDEA, Maven and Docker.
- **AI assistance:** Helped identify mismatched Java versions and recommended using JDK 25 and Maven Wrapper.
- **Developer work:** Installed Eclipse Temurin JDK 25.0.5 and IntelliJ IDEA 2026.2.3, configured the JDK, activated the educational license and started Docker Desktop.
- **Changes and decisions:** Kept older JDK installations and used Maven Wrapper instead of installing Maven globally.
- **Verification:** Java commands reported JDK 25.0.5, Maven Wrapper reported 3.10.0 and Docker Engine was running.
- **Files:** Local development environment.

### AI-006 — Generate Spring Boot project

- **Date:** 2026-10-29.
- **Task:** Generate the application foundation using Spring Boot 4.1.2 and Java 25.
- **AI assistance:** Helped select Spring Web MVC, Thymeleaf, Spring Data JPA, PostgreSQL Driver, Flyway and Validation. Reviewed the generated project structure and Maven configuration.
- **Developer work:** Generated the application through IntelliJ and kept the existing repository and documentation directories.
- **Changes and decisions:** Corrected the project destination to avoid creating a nested repository. Used the generated Maven Wrapper.
- **Verification:** `./mvnw --version` confirmed Maven 3.10.0 and Java 25.0.5. `./mvnw -DskipTests package` returned `BUILD SUCCESS`. Tests were skipped.
- **Files:** `pom.xml`, Maven Wrapper files, Spring Boot application class and generated test class.
- **Git commit:** `3612469`.

### AI-007 — Configure PostgreSQL with Docker Compose

- **Date:** 2026-10-29.
- **Task:** Configure a PostgreSQL development database.
- **AI assistance:** Suggested PostgreSQL 17, Docker Compose, a persistent named volume, health check, local port binding and environment-based credentials.
- **Developer work:** Created `compose.yaml`, `.env.example`, local `.env` and Git ignore rules. Started the container and tested the connection.
- **Changes and decisions:** Bound PostgreSQL to `127.0.0.1:5433`. A real password was initially placed in `.env.example`; the developer identified the problem, replaced it with a placeholder and rewrote Git history.
- **Verification:** Docker Compose validation passed, the container reported healthy and PostgreSQL returned the expected database, user and version 17.11. The password was not rotated, so it must still be considered potentially exposed.
- **Files:** `compose.yaml`, `.env.example`, `.gitignore`.
- **Git commit:** `0001f35` after the history rewrite.

### AI-008 — Connect Spring Boot to PostgreSQL

- **Date:** 2026-10-29.
- **Task:** Configure database connectivity and verify application startup.
- **AI assistance:** Suggested environment-based datasource properties, Hibernate schema validation, disabling Open Session in View and enabling Flyway.
- **Developer work:** Updated `application.properties`, configured the local `DB_PASSWORD` variable and launched the application from Git Bash and IntelliJ.
- **Changes and decisions:** Kept the database password outside tracked files and stored the IntelliJ runtime configuration locally.
- **Verification:** Startup logs showed successful HikariCP connections, Flyway initialization, Hibernate initialization and Tomcat on port 8080. `curl` returned HTTP 404 at `/`, expected because no homepage had been implemented.
- **Files:** `src/main/resources/application.properties`.
- **Git commit:** `77fbe5a`.

### AI-009 — Create decision register and AI usage log

- **Date:** 2026-10-29.
- **Task:** Record accepted project decisions and previous development work.
- **AI assistance:** Prepared initial Markdown drafts from the requirements, agreed architecture and available verification evidence.
- **Developer work:** Reviewed both drafts, requested simpler writing and removed unnecessary prompt examples before committing them.
- **Changes and decisions:** Kept the documents focused on decisions, development contributions and verifiable results.
- **Verification:** Both files were committed to GitHub.
- **Files:** `docs/decisions.md`, `docs/ai-usage.md`.
- **Git commits:** `bdc2cbc`, `a93be36`.

### AI-010 — Select AI provider

- **Date:** 2026-10-29.
- **Task:** Compare suitable AI providers for classifying purchase suggestions and identifying possible duplicate book titles.
- **AI assistance:** Compared Google Gemini, OpenAI, Anthropic and other approaches against the P03 requirements.
- **Developer work:** Requested an additional comparison with the professor's assignment and selected Google Gemini as the provider.
- **Changes and decisions:** Chose a lightweight structured-output workflow instead of training a custom model. The application will supply valid categories and existing catalog candidates to the provider. Gemini 2.5 Flash-Lite is the provisional model.
- **Verification:** Provider selection was recorded. No live Gemini API requests, model evaluations or quota tests have been completed.
- **Files:** `docs/decisions.md`; ADR-002 planned.

### AI-011 — Select hosting and managed database

- **Date:** 2026-10-29.
- **Task:** Choose a deployment approach that meets the course requirements without project expenses.
- **AI assistance:** Compared hosting options and examined the professor's deployment recommendations, including limitations of sleeping free instances.
- **Developer work:** Established a €0 budget with no payment card and accepted Render Free and Neon Free.
- **Changes and decisions:** Planned Docker-based deployment to Render with PostgreSQL hosted by Neon. Proposed a short demonstration interval for scheduled jobs without changing calendar-day fine calculations.
- **Verification:** Hosting choices were recorded. Neither service has been deployed or tested. Reliable execution of scheduled jobs during Render sleep remains unresolved.
- **Files:** `docs/decisions.md`; deployment configuration planned.

### AI-012 — Prepare M1 Figma wireframes

- **Date:** 2026-10-29 to 2026-10-30.
- **Task:** Prepare low-fidelity screens for the library system.
- **AI assistance:** Helped interpret the Figma requirements, reduce the screen inventory and write a prompt for Figma AI Agent. Reviewed screenshots of the generated designs.
- **Developer work:** Activated Figma Education, created the design file and its pages, ran the generation prompt, inspected the editable elements and exported the wireframes as PDFs.
- **Changes and decisions:** Reduced the initial scope to 11 main layouts and three state variations. Kept the designs grayscale and reserved components and high-fidelity screens for M2.
- **Verification:** Screenshots showed the main layouts, state variations and editable Figma layers. Four exported PDF files are present in the GitHub repository. Full PDF content and public-link accessibility were not reverified during this update.
- **Files:** Figma prototype and `docs/wireframes-m1/`.
- **Figma link:** https://www.figma.com/design/PiQSnO17vQGKHeqz3ktlb1/P03-Library-Lending-System---UI-UX-Prototype

## M2 — Database migrations

### AI-013 — Implement initial Flyway schema

- **Date:** 2026-11-05.
- **Task:** Convert the approved ER diagram into an initial PostgreSQL migration.
- **AI assistance:** Drafted `V1__create_library_schema.sql` for the 12 application tables and reviewed its constraints and relationships.
- **Developer work:** Created the file in IntelliJ, applied it through Spring Boot and checked the database with PostgreSQL commands. Requested a second review before committing.
- **Changes and decisions:** Corrected `ReturbedById` to `ReturnedById` and `FullfilledAt` to `FulfilledAt` in the SQL. Kept the manually prepared M1 ERD unchanged. Deferred additional reservation safeguards to V2.
- **Verification:** Flyway applied V1 successfully. PostgreSQL showed 12 application tables and `flyway_schema_history`. The primary-category, active-loan and daily-fine uniqueness indexes were confirmed. JPA entities had not been created.
- **Files:** `src/main/resources/db/migration/V1__create_library_schema.sql`.
- **Git commit:** `8ec9550`.

### AI-014 — Strengthen database integrity

- **Date:** 2026-11-05.
- **Task:** Review the initial database and implement additional reservation and return constraints.
- **AI assistance:** Identified missing safeguards and prepared `V2__strengthen_reservation_constraints.sql`. Added status checks, timestamp consistency, partial unique indexes, a composite copy/title foreign key and a loan-return consistency check. Prepared six SQL integrity tests.
- **Developer work:** Requested a full architectural review before implementation, applied V2, inspected the database constraints and ran the tests.
- **Changes and decisions:** Retained historical reservation information while preventing duplicate active allocations. Kept FIFO allocation, renewal eligibility and other cross-record rules in the planned Java service layer.
- **Verification:** Flyway applied V2 successfully. PostgreSQL schema inspection showed the expected constraints. The developer reported all six negative integrity tests passing; the individual test-output transcript was not provided.
- **Files:** `src/main/resources/db/migration/V2__strengthen_reservation_constraints.sql`.
- **Remaining work:** Transactional services and automated concurrency tests.

### AI-015 — Seed application reference data

- **Date:** 2026-11-05.
- **Task:** Add the initial user roles and library subject categories.
- **AI assistance:** Proposed a Flyway migration containing permanent reference data, with demonstration data kept separate.
- **Developer work:** Approved the category list, created V3, ran Spring Boot and verified the inserted records directly in PostgreSQL.
- **Changes and decisions:** Inserted three roles and ten categories. No users, passwords, loans or time-dependent demonstration records were included.
- **Verification:** Flyway history showed versions 1, 2 and 3 applied successfully. PostgreSQL queries confirmed all three roles, ten categories and matching record counts. V3 was committed to GitHub.
- **Files:** `src/main/resources/db/migration/V3__seed_reference_data.sql`.

## Verification summary

### M1 — 2026-10-29 to 2026-10-30

| Area | Verified result |
|---|---|
| Development environment | Java 25.0.5, IntelliJ IDEA 2026.2.3 and Maven Wrapper 3.10.0. |
| Build | Maven package completed successfully with tests skipped. |
| PostgreSQL | Docker container healthy; version 17.11 and expected database/user confirmed. |
| Spring Boot | Database connection and startup verified on port 8080. |
| HTTP | Root endpoint returned 404 before implementing controllers. |
| Git | Repository history and remote synchronization checked during M1. |
| Design | 11 wireframe layouts and three state variations reviewed; four PDF exports saved in GitHub. |

### M2 — 2026-11-05

| Area | Verified result |
|---|---|
| V1 | 12 application tables created; selected uniqueness indexes inspected. |
| V2 | Reservation and loan-return constraints inspected in PostgreSQL. |
| V2 tests | Six manual negative SQL tests reported passing; full output not independently reviewed. |
| V3 | Three roles and ten categories verified through SQL queries. |
| Git | All three migration files present on GitHub `main`. |

## Remaining work

The database migrations and permanent reference data are in place. JPA entities, repositories, services, CRUD screens and development-only fixtures have not yet been implemented.

Automated Java tests, security, REST API, Gemini integration, scheduling, internationalization, deployment, performance measurements and service coverage are still pending. No test coverage, AI evaluation scores or deployment results have been claimed.

The M2 high-fidelity Figma prototype, components, responsive layouts and interactive flow also remain to be completed.
