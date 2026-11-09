
# AI Usage Log

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Started:** 2026-10-29  
**Last updated:** 2026-11-09

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
- **Changes and decisions:** Bound PostgreSQL to `127.0.0.1:5433`. A real password was initially placed in `.env.example`.
- **Verification:** Docker Compose validation passed, the container reported healthy and PostgreSQL returned the expected database, user and version 17.11.
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

## M2 — Catalog persistence and continuous integration

### AI-016 — Implement catalog JPA entities

- **Date:** 2026-11-05 to 2026-11-06.
- **Task:** Map the initial catalog portion of the PostgreSQL schema to Java entities.
- **AI assistance:** Proposed and reviewed JPA mappings for book titles, physical copies, categories and the attributed book-category association. Explained lazy relationships, composite keys, `@MapsId`, entity lifecycle callbacks and parent-child association management.
- **Developer work:** Created the Java classes in IntelliJ, checked compilation and application startup, reviewed the persistence design and committed the implemented entities.
- **Changes and decisions:** Implemented `BookTitle`, `BookCopy`, `Category`, `BookCategory` and `BookCategoryId`. Used an explicit association entity for the attributed many-to-many relationship. Kept entity mappings aligned with the existing Flyway schema rather than changing applied migrations.
- **Verification:** Maven compilation and Spring Boot startup were reported successful. Subsequent repository integration tests verified persistence and relationships against PostgreSQL.
- **Files:** `src/main/java/hr/algebra/librarylendingsystem/domain/`.
- **Remaining work:** Map the other eight application tables and implement service-level business operations.

### AI-017 — Add catalog repositories and integration tests

- **Date:** 2026-11-06.
- **Task:** Implement catalog repositories and verify persistence against PostgreSQL.
- **AI assistance:** Proposed four Spring Data JPA repository interfaces and three integration tests. Helped diagnose a missing `DB_PASSWORD` environment variable in IntelliJ JUnit configurations and explained how to supply it securely in Git Bash.
- **Developer work:** Created the repository interfaces and integration test class, configured database credentials locally, ran the individual tests, ran the full Maven test suite and corrected the IntelliJ Run All Tests configuration.
- **Changes and decisions:** Kept tests transactional and used unique inventory codes for test data. Tested book title/copy persistence, attributed category associations and rejection of two primary categories for one title.
- **Verification:** All three catalog integration tests passed. The full four-test suite also passed through Maven and IntelliJ. The third test confirmed that PostgreSQL rejects a second primary category for the same title.
- **Files:** `src/main/java/hr/algebra/librarylendingsystem/repository/`, `src/test/java/hr/algebra/librarylendingsystem/CatalogRepositoryIntegrationTests.java`.
- **Remaining work:** Add service unit tests, MVC tests, `@DataJpaTest` coverage, Testcontainers integration tests and JaCoCo service coverage as development progresses.

### AI-018 — Configure GitHub Actions continuous integration

- **Date:** 2026-11-06.
- **Task:** Implement automated build and test verification for the Spring Boot application.
- **AI assistance:** Reviewed the professor's engineering and milestone requirements, inspected the committed Maven and datasource configuration and prepared a GitHub Actions workflow. Explained PostgreSQL service containers, Java setup, environment variables, Maven verification and CI status badges.
- **Developer work:** Created `.github/workflows/ci.yml` locally in IntelliJ, committed and pushed the workflow, inspected the GitHub Actions execution and reported the result.
- **Changes and decisions:** Configured the workflow to run on pushes and pull requests using Ubuntu, Eclipse Temurin Java 25 and PostgreSQL 17. The workflow supplies isolated test database credentials and overrides the local datasource URL to use port 5432. GitHub Actions permissions are limited to repository read access.
- **Verification:** The developer reported that the GitHub Actions job completed with `BUILD SUCCESS` and all four tests passing. The workflow configuration was subsequently inspected in the committed GitHub repository.
- **Files:** `.github/workflows/ci.yml`.
- **Remaining work:** Extend automated verification as more test categories are implemented. The CI badge is now present in the README.

## M2 — High-fidelity Figma design and prototype

### AI-019 — Build reusable Figma components

- **Date:** M2 design phase, reviewed by 2026-11-09.
- **Task:** Create a reusable component library for the Spring MVC application designs.
- **AI assistance:** Prepared Figma Design AI prompts for reusable buttons, inputs, selects, status badges, alerts, navigation items, catalog rows and category badges. Explained the difference between reusable Figma components and decorative groups.
- **Developer work:** Ran the prompts in Figma, reviewed the generated component library and retained the approved design system.
- **Changes and decisions:** Established forest green, warm ivory and muted gold as the principal colors. Reused Inter typography, spacing conventions and native component variants. Preserved the M1 wireframes on a separate page.
- **Verification:** The Figma component structure was inspected through the connected Figma API. Eight native component sets and 36 component variants were identified. All 36 variants had Auto Layout configured at the time of inspection.
- **Files:** Figma page `02 - Components`.
- **Limitations:** Component design verification does not establish that the corresponding HTML and CSS components have been implemented.

### AI-020 — Generate and review high-fidelity desktop screens

- **Date:** M2 design phase, reviewed by 2026-11-09.
- **Task:** Prepare the required high-fidelity librarian screens and application state variations.
- **AI assistance:** Developed sequential Figma Design AI prompts for catalog management, title editing, physical-copy details, empty results, validation errors and AI-assisted purchase suggestion review. Reviewed the generated results and recommended refinements.
- **Developer work:** Executed the prompts in Figma, checked their visual consistency and accepted the completed screens.
- **Changes and decisions:** Kept title metadata separate from physical-copy inventory. Preserved entered form values in the validation-error design. Designed the AI purchase-suggestion screen so Gemini recommendations are advisory and the librarian controls category overrides and acquisition decisions.
- **Verification:** Six desktop frames, each 1440px wide, were confirmed through Figma metadata and screenshots. The AI-assisted review frame included 17 component instances. The other approved screens were retained.
- **Files:** Figma page `03 - High-Fidelity Screens`.
- **Limitations:** The designs contain illustrative catalog and Gemini data. They do not demonstrate working persistence, validation or AI integration.

### AI-021 — Create and verify responsive mobile screens

- **Date:** 2026-11-09.
- **Task:** Adapt the approved book catalog and edit form to 390px mobile layouts.
- **AI assistance:** Prepared a Figma Design AI prompt specifying mobile navigation, stacked catalog cards, single-column form controls, category selection, responsive spacing and accessibility considerations.
- **Developer work:** Ran the prompt in the existing Figma design conversation and reviewed the generated mobile screens.
- **Changes and decisions:** Replaced the desktop sidebar with a compact mobile header. Represented catalog rows as vertically stacked cards while preserving physical-copy availability information. Kept all required form fields and primary-category selection controls.
- **Verification:** Both mobile frames were found within the `Mobile responsive variants · 390px` section. Their widths were confirmed as 390px. The catalog frame contained 23 component instances and 61 Auto Layout containers. The edit form contained 12 component instances and 60 Auto Layout containers. No text layers below 12px were found during the inspection.
- **Files:** Figma frames `Book Catalog — Mobile 390` and `Edit Book Title — Mobile 390`.
- **Limitations:** Browser reflow and mobile interaction behavior remain to be tested in the implemented application.

### AI-022 — Connect and test the clickable Figma prototype

- **Date:** 2026-11-09.
- **Task:** Create an interactive librarian catalog-management flow using the approved desktop designs.
- **AI assistance:** Prepared an interaction plan and attempted a Figma Design AI prompt. When the Figma agent reported that it could not create native prototype connections, provided step-by-step instructions for using Figma's Prototype tab manually.
- **Developer work:** Created the prototype connections in Figma and tested the navigation in presentation mode.
- **Changes and decisions:** Used existing frames instead of generating replacement screens. The main flow connects Book Catalog, Book Title Details and Edit Book Title. Save returns to the title details screen with a displayed success notification. Cancel and breadcrumb navigation were also planned and connected as part of the tested workflow.
- **Verification:** The developer reported that the prototype interactions worked correctly. Native Figma navigation demonstrates the intended workflow without saving records to a database.
- **Files:** Figma page `03 - High-Fidelity Screens`.
- **Limitations:** The static prototype does not execute server-side validation or persist data. No automated Figma interaction test was performed.

### AI-023 — Audit design contrast and prepare the rationale

- **Date:** 2026-11-09.
- **Task:** Review Figma color contrast and document the UI/UX design decisions.
- **AI assistance:** Used read-only Figma inspection to extract text and background colors, inspected relevant component styles and calculated WCAG contrast ratios using relative luminance. Drafted the design rationale.
- **Developer work:** Requested numerical evidence rather than a visual estimate, reviewed the findings and created `docs/design-rationale.md` locally.
- **Changes and decisions:** Kept the established design palette while documenting restrictions on low-contrast decorative colors. Distinguished Figma contrast measurements from complete accessibility conformance.
- **Verification:** All 20 distinct text/background combinations extracted from eight high-fidelity frames met the 4.5:1 normal-text contrast threshold. The standard input border measured approximately 3.64:1 against white, and the primary focus outline measured approximately 4.77:1 against white.
- **Files:** `docs/design-rationale.md` and the Figma component library.
- **Limitations:** Some subtle borders, pale focus halos and gold accents fall below 3:1 against light backgrounds and must not be the sole meaningful interface indicators. Keyboard navigation, screen-reader behavior and browser-rendered accessibility remain untested.

### AI-024 — Export prototype PDF and update documentation

- **Date:** 2026-11-09.
- **Task:** Prepare the M2 high-fidelity prototype export and update repository documentation.
- **AI assistance:** Explained Figma's multipage PDF export process, identified the risk of omitting mobile frames nested in a section, reviewed screenshots of the exported mobile pages and prepared updated README and AI usage documentation.
- **Developer work:** Exported the Figma designs, confirmed that the resulting PDF contained eight pages and provided screenshots showing the two mobile frames as the final pages. Prepared the prototype PDF and design rationale for inclusion in the project documentation.
- **Changes and decisions:** Kept the four M1 wireframe PDFs separate from the M2 prototype PDF. Planned `docs/prototype.pdf` as the combined high-fidelity export, with the README linking to both the prototype and design rationale.
- **Verification:** The developer confirmed an eight-page PDF. Screenshots of the final two pages were inspected and showed the complete mobile catalog and edit form. The PDF file itself was not independently opened and inspected during this review.
- **Files:** `docs/prototype.pdf`, `README.md`, `docs/ai-usage.md`.
- **Remaining work:** Save and review the updated documentation, verify Figma sharing permissions, commit and push the files, then confirm their presence on GitHub.

## Verification summary

### M1 — 2026-10-29 to 2026-10-30

| Area | Verified result |
|---|---|
| Development environment | Java 25.0.5, IntelliJ IDEA 2026.2.3 and Maven Wrapper 3.10.0. |
| Build | Initial Maven package completed successfully with tests skipped. |
| PostgreSQL | Docker container healthy; version 17.11 and expected database/user confirmed. |
| Spring Boot | Database connection and startup verified on port 8080. |
| HTTP | Root endpoint returned 404 before implementing controllers. |
| Git | Repository history and remote synchronization checked during M1. |
| Design | 11 wireframe layouts and three state variations reviewed; four PDF exports saved in GitHub. |

### M2 — 2026-11-05 to 2026-11-09

| Area | Verified result |
|---|---|
| V1 | 12 application tables created; selected uniqueness indexes inspected. |
| V2 | Reservation and loan-return constraints inspected in PostgreSQL. |
| V2 tests | Six manual negative SQL tests reported passing; full output not independently reviewed. |
| V3 | Three roles and ten categories verified through SQL queries. |
| JPA mappings | Five catalog domain classes created and committed. |
| Repositories | Four Spring Data JPA repositories created and committed. |
| Integration tests | Three catalog integration tests passed against PostgreSQL. |
| Local test suite | Four tests passed through Maven and IntelliJ. |
| Continuous integration | GitHub Actions build succeeded with four passing tests, as reported by the developer. |
| Git | Migration, domain, repository, test and CI workflow files are present on GitHub `main`. |
| Figma components | Eight native component sets and 36 variants inspected. |
| Desktop designs | Six high-fidelity desktop frames verified at 1440px width. |
| Mobile designs | Two high-fidelity mobile frames verified at 390px width. |
| Prototype | Main librarian navigation flow reported working in Figma presentation mode. |
| Contrast | Twenty extracted text/background combinations met the 4.5:1 threshold. |
| Design rationale | Drafted and added locally by the developer. |
| Prototype PDF | Eight pages confirmed by the developer; final two mobile pages visually inspected. |
| Documentation | README and AI usage log prepared for local review and commit. |

## Remaining work

The database migrations, permanent reference data, initial catalog JPA mappings, repositories, integration tests and GitHub Actions CI workflow are in place.

The remaining application tables still require Java mappings and repositories. Catalog services, DTOs, validation, CRUD screens and development-only fixtures have not yet been implemented.

Security, REST API, Gemini integration, scheduling, internationalization, deployment, performance measurements, additional test categories and service coverage remain pending. No coverage percentages, AI evaluation scores or production deployment results have been claimed.

The M2 Figma design work has been prepared, including reusable components, desktop and mobile screens, a clickable prototype, contrast measurements, design rationale and PDF export.

Final Figma documentation checks, repository synchronization and public sharing verification remain pending. The implemented application's accessibility and functional behavior must be tested separately from the Figma prototype.
