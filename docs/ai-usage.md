# AI Usage Log

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks (2026/27)  
**Started:** 2026-10-29  
**Last updated:** 2026-11-14

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

## M2 — Book Title management and Book Copy validation

### AI-025 — Implement Book Title details, editing and navigation

- **Date:** 2026-11-11 to 2026-11-12.
- **Task:** Complete Book Title details and editing workflows, connect the catalog management screens and add automated MVC tests.
- **AI assistance:** Proposed the Details and Edit controller endpoints, Thymeleaf templates, validation handling, redirects and navigation links. Prepared MVC tests for book details, missing titles, edit-form prepopulation, successful updates and rejected submissions. Helped refine the action-button spacing.
- **Developer work:** Implemented the changes locally, tested Book Title details and editing through the browser, checked optional metadata and inactive titles, verified responsive layouts and corrected the details-page action buttons. Ran the Maven test suite, reviewed the results and committed the completed implementation.
- **Changes and decisions:** Reused `BookTitleForm` and `BookTitleCommand` for Create and Edit. Kept business rules in the service layer. Updated Create to redirect to the newly created title's details page. Added catalog navigation, Edit actions and success messages. Preserved existing title status during metadata editing.
- **Verification:** The developer reported all manual workflow checks passing and 42 automated tests passing after completing the Book Title management implementation. The final commit was inspected on GitHub and contained the controller, templates, CSS and MVC test changes. The reported local test results were not independently executed by the AI.
- **Files:** `BookTitleManagementController.java`, `BookTitleManagementControllerTests.java`, `templates/catalog/list.html`, `templates/catalog/details.html`, `templates/catalog/edit.html`, `static/css/library.css` and `static/css/book-title-details.css`.
- **Git commit:** `9990882` (`implement and test book title details and editing`).
- **Remaining work:** Implement physical-copy management, circulation workflows, authorization and related integration tests.

### AI-026 — Define and validate Book Copy conditions and statuses

- **Date:** 2026-11-12.
- **Task:** Establish accepted physical-copy conditions and circulation statuses as the first checkpoint of Book Copy management.
- **AI assistance:** Reviewed the existing `BookCopy` entity, repository, Flyway schema and decision register. Proposed five physical conditions and five circulation statuses, explained their separation and prepared Java enums, entity validation and focused unit tests.
- **Developer work:** Reviewed and accepted the proposed values, implemented the Java classes and validation changes, ran the focused tests and executed the full Maven verification. Confirmed that all 51 tests passed.
- **Changes and decisions:** Accepted `NEW`, `GOOD`, `FAIR`, `POOR` and `DAMAGED` as physical conditions. Accepted `AVAILABLE`, `ON_LOAN`, `ON_HOLD`, `MAINTENANCE` and `WITHDRAWN` as circulation statuses. Retained string-based JPA fields and existing database columns to avoid unnecessary changes to working catalog queries and integration tests. Added inventory-code normalization and validation.
- **Verification:** Nine new unit tests covered supported and unsupported condition/status values, missing values, inventory-code validation, normalization and setter behavior. The developer reported all 51 tests passing with `./mvnw verify`. No database migration or new database constraint was applied.
- **Files:** `src/main/java/hr/algebra/librarylendingsystem/domain/BookCopy.java`, `src/main/java/hr/algebra/librarylendingsystem/domain/BookCopyCondition.java`, `src/main/java/hr/algebra/librarylendingsystem/domain/BookCopyStatus.java` and `src/test/java/hr/algebra/librarylendingsystem/BookCopyValidationTests.java`.
- **Remaining work:** Complete circulation-aware status transitions and database allowed-value constraints.
- **Git status:** Step 19.1A was locally verified at this checkpoint; it was subsequently included in the committed Book Copy service work.

### AI-027 — Implement Book Copy service and integration tests

- **Date:** 2026-11-12.
- **Task:** Implement the physical-copy service for creation, retrieval, metadata updates, withdrawal, safe deletion and maintenance release.
- **AI assistance:** Prepared service interfaces, DTOs, exceptions, repository queries and integration tests. Reviewed the inventory rules and identified an inconsistency in handling copies with `POOR` condition.
- **Developer work:** Implemented the service classes and tests in IntelliJ, corrected the `DuplicateInventoryCodeException` filename, fixed the `POOR` condition behavior and ran Maven verification after each development step.
- **Changes and decisions:** Kept inventory updates separate from loan and reservation operations. Added validation for inventory codes, restrictions on protected circulation statuses, historical-reference checks and pessimistic locking for administrative operations. Maintenance release requires an eligible condition and checks for active loans, allocated reservations and waiting reservations.
- **Verification:** Seven creation/retrieval tests, eight update tests, ten lifecycle tests and nine maintenance tests were added. The developer reported 85 passing tests after Step 19.1E, followed by successful regression verification of the corrected `POOR` behavior. The completed service checkpoint was committed and pushed, and the developer reported passing CI.
- **Files:** `BookCopyService.java`, `BookCopyServiceImpl.java`, `BookCopyCommand.java`, `BookCopyDetails.java`, `BookCopyRepository.java`, `BookCopyNotFoundException.java`, `InvalidBookCopyException.java`, `DuplicateInventoryCodeException.java`, `BookCopyServiceIntegrationTests.java`, `BookCopyUpdateIntegrationTests.java`, `BookCopyLifecycleIntegrationTests.java` and `BookCopyMaintenanceIntegrationTests.java`.
- **Remaining work:** Implement full circulation-aware transitions, concurrency tests and the other application workflows.
- **Git commit:** `e3fc57a` (`implement and test physical book copy management service`).

### AI-028 — Implement Physical Copy management screens and actions

- **Date:** 2026-11-12 to 2026-11-13.
- **Task:** Complete librarian-facing Physical Copy management with inventory listing, creation, editing, maintenance release, withdrawal and safe deletion.
- **AI assistance:** Proposed the Spring MVC controller endpoints, Thymeleaf forms, inventory table, confirmation screens and associated MVC and integration tests. Helped identify and correct future acquisition-date acceptance, duplicate field-error messages and the visual appearance of the Delete action.
- **Developer work:** Implemented the changes in IntelliJ, reviewed the application against the approved Figma design, disabled Dark Reader for the local application after identifying its CSS overrides and verified the actual color palette. Tested the inventory workflow in the browser, including validation, restricted actions, confirmation pages and mobile scrolling. Ran the Maven test suite repeatedly, committed and pushed the completed inventory-management work.
- **Changes and decisions:** Reused Book Copy service operations and DTOs instead of putting business rules in controllers. Kept creation and editing separate from circulation. Made acquisition dates optional but not future-dated. Added confirmation pages for withdrawal and deletion and POST endpoints for state changes. Kept the horizontally scrollable mobile inventory table instead of adding separate cards. Used neutral styling for Edit and Withdraw, green for Release and red for Delete.
- **Verification:** The developer reported successful automated tests and manual checks for inventory listing, Add, Edit, Release, Withdraw and Delete. Manual checks included status changes, inventory counts, error feedback, cancellation, protected statuses and responsive layout. The developer confirmed that the final red Delete button styling was corrected and that the full test suite passed. The completed implementation was pushed, and GitHub Actions passed as reported by the developer.
- **Limitations:** A read-only join between `BookCopies` and `Loans` returned no rows in the local development database. Therefore, deletion rejection due specifically to existing loan history was not demonstrated manually. Service integration tests cover historical-reference restrictions. Concurrent circulation operations and authorization remain future work.
- **Files:** `BookCopyManagementController.java`, `BookCopyForm.java`, `BookCopyService.java`, `BookCopyServiceImpl.java`, `BookTitleManagementController.java`, `BookTitleManagementControllerTests.java`, `BookCopyManagementControllerTests.java`, `BookCopyEditControllerTests.java`, `BookCopyReleaseControllerTests.java`, `BookCopyWithdrawControllerTests.java`, `BookCopyDeleteControllerTests.java`, `BookCopyAcquisitionDateIntegrationTests.java`, `BookCopyLookupIntegrationTests.java`, `templates/catalog/details.html`, `templates/catalog/copies/create.html`, `templates/catalog/copies/edit.html`, `templates/catalog/copies/withdraw.html`, `templates/catalog/copies/delete.html`, `static/css/book-copy-list.css` and `static/css/book-copy-actions.css`.
- **Git commit:** `93d529d` (`complete and document physical copy management`).
- **Remaining work:** Review the completed parent-child CRUD workflows against the professor's M2 requirements.

## M2 — Remaining persistence mappings

### AI-029 — Implement User and Role persistence

- **Date:** 2026-11-13.
- **Task:** Implement the remaining user and role JPA mappings, repositories and persistence integration tests.
- **AI assistance:** Reviewed the existing `AppUsers`, `Roles` and `UserRoles` tables and proposed the `AppUser` and `Role` entities, repository methods and integration tests. Explained case-insensitive email uniqueness, lazy role associations and loading roles for future authentication.
- **Developer work:** Implemented the entities, repositories and tests in IntelliJ. Reviewed the mappings against the existing Flyway schema, ran Maven verification and committed the completed step.
- **Changes and decisions:** Used a many-to-many relationship through the existing `UserRoles` table without creating an unnecessary association entity. Normalized user emails to lowercase and used an entity graph to retrieve users together with their roles when required.
- **Verification:** Eight repository integration tests passed, covering user persistence, email lookup, duplicate email rejection, seeded roles, multiple role assignments, association removal and entity-graph loading. The developer reported successful local verification and GitHub Actions passed after the commit was pushed.
- **Files:** `AppUser.java`, `Role.java`, `AppUserRepository.java`, `RoleRepository.java`, `AppUserRepositoryIntegrationTests.java`.
- **Git commit:** `9838fdd` (`implement and test user and role persistence`).
- **Remaining work:** Implement authentication, password hashing, authorization and account management services.

### AI-030 — Implement circulation persistence mappings

- **Date:** 2026-11-13.
- **Task:** Implement JPA mappings and repositories for reservations, loans and daily fine accruals.
- **AI assistance:** Reviewed the existing database schema and constraints, prepared entity mappings and repository methods, and proposed integration tests for persistence, uniqueness constraints and optimistic locking. Discussed the difference between database integrity and future transactional circulation operations.
- **Developer work:** Implemented the classes and tests in IntelliJ, ran the full Maven verification and committed the completed persistence step.
- **Changes and decisions:** Introduced `ReservationStatus`, `Reservation`, `Loan` and `FineAccrual`. Used lazy relationships, separate issuing and returning librarian references, `BigDecimal` for fine amounts and `@Version` for optimistic locking on loans. Preserved the existing Flyway schema.
- **Verification:** Nine integration tests passed, covering reservation persistence and FIFO ordering, duplicate active reservations, loan persistence, duplicate active loans, optimistic-lock version updates, stale-update rejection, fine accrual persistence and duplicate daily fine rejection. The developer reported successful local verification and GitHub Actions passed after the commit was pushed.
- **Files:** `ReservationStatus.java`, `Reservation.java`, `Loan.java`, `FineAccrual.java`, `ReservationRepository.java`, `LoanRepository.java`, `FineAccrualRepository.java`, `CirculationRepositoryIntegrationTests.java`.
- **Git commit:** `54775c2` (`implement and test circulation persistence mappings`).
- **Remaining work:** Implement checkout, return, renewal, reservation allocation, fine calculation and scheduled processing services.

### AI-031 — Implement Purchase Suggestion and AI Result persistence

- **Date:** 2026-11-14.
- **Task:** Complete the remaining JPA mappings and repositories for purchase suggestions and their associated AI analysis results.
- **AI assistance:** Reviewed the existing Flyway tables, entity conventions and repository implementations. Prepared `PurchaseSuggestion` and `AiSuggestionResult` entities, status enums, repository methods and eight PostgreSQL integration tests. Helped diagnose JSON mapping compatibility and a timestamp-precision test failure.
- **Developer work:** Implemented the classes in IntelliJ and ran Maven verification after each step. Reported that the initial Jackson `JsonNode` import could not be resolved. Requested a correction based on the project's actual dependencies and existing conventions. Implemented the revised JSONB mapping and repository tests, corrected the timestamp assertion and repeated full verification.
- **Changes and decisions:** Used `PurchaseSuggestionStatus` values `PENDING`, `APPROVED` and `REJECTED`, and `AiProcessingStatus` values `PENDING`, `COMPLETED` and `FAILED`. Retained optional reviewer information and separate AI processing status. Replaced the initial Jackson 2 `JsonNode` approach with `List<Map<String, Object>>` and Hibernate's `@JdbcTypeCode(SqlTypes.JSON)` for PostgreSQL JSONB columns. No new dependencies or Flyway migration were required.
- **Verification:** Eight integration tests were added for purchase suggestion persistence, member history, status filtering, librarian review information, pending AI results, JSONB serialization and retrieval, multiple analysis results and processing-status queries. The initial run had one failed assertion because PostgreSQL stored the review timestamp with microsecond precision. The test timestamp was truncated to microseconds, and the developer subsequently reported all 158 tests passing with `./mvnw clean verify`. The implementation was committed and pushed, and the developer reported that GitHub Actions passed.
- **Files:** `PurchaseSuggestion.java`, `PurchaseSuggestionStatus.java`, `AiSuggestionResult.java`, `AiProcessingStatus.java`, `PurchaseSuggestionRepository.java`, `AiSuggestionResultRepository.java`, `PurchaseSuggestionRepositoryIntegrationTests.java`.
- **Git commit:** `507ec60` (`implement and test purchase suggestion and AI result persistence`).
- **Remaining work:** Implement member submission, AI processing, librarian review, provider integration and deterministic AI stub tests.

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

### M2 — 2026-11-11 to 2026-11-12

| Area | Verified result |
|---|---|
| Book Title management | Create, Details and Edit workflows manually tested by the developer. |
| Catalog navigation | Desktop and mobile title links, Add Book Title action and Edit navigation verified manually. |
| Form validation | Required fields, primary-category selection and invalid updates tested. |
| Missing titles | HTTP 404 behavior tested for Details and Edit requests. |
| Optional metadata | Missing ISBN, publisher, publication year and description displayed correctly. |
| Responsive layout | Catalog and management screens reviewed at desktop, tablet and mobile widths. |
| Book Title MVC tests | Existing tests extended to cover details and editing. |
| Book Title test suite | 42 tests reported passing after completing Step 18. |
| Book Title Git commit | Commit `9990882` confirmed on GitHub with seven changed files. |
| Book Copy values | Five conditions and five statuses accepted and implemented in Java. |
| Book Copy validation | Nine focused unit tests reported passing. |
| Full Maven verification | 51 tests reported passing after Step 19.1A. |
| Database changes | No new migration introduced for Step 19.1A. |
| Book Copy Git status | Changes subsequently included in the committed service checkpoint. |

### M2 — Physical-copy service verification, 2026-11-12

| Area | Verified result |
|---|---|
| Copy creation and retrieval | Seven PostgreSQL integration tests reported passing. |
| Copy metadata updates | Eight PostgreSQL integration tests reported passing. |
| Withdrawal and safe deletion | Ten lifecycle integration tests reported passing. |
| Maintenance release | Nine integration tests reported passing. |
| Historical integrity | Tests covered references from loans and reservations. |
| Pessimistic locking | Implemented for administrative copy modifications; concurrent circulation integration remains untested. |
| Build after Step 19.1B | 58 tests reported passing. |
| Build after Step 19.1C | 66 tests reported passing. |
| Build after Step 19.1D | 76 tests reported passing. |
| Build after Step 19.1E | 85 tests reported passing. |
| Final review | `POOR` condition handling corrected and regression-tested. |
| Database migrations | No new Flyway migrations introduced. |
| Git | Service implementation committed and pushed; CI passed as reported by the developer. |

### M2 — Physical-copy web verification, 2026-11-12 to 2026-11-13

| Area | Verified result |
|---|---|
| Inventory listing | Physical-copy details, statuses and inventory counts displayed on Book Title Details. |
| Add Physical Copy | Form, required fields, optional date and service-derived initial status manually verified. |
| Acquisition-date validation | Future dates rejected in creation and editing; regression tests reported passing. |
| Edit Physical Copy | Prefilled form, metadata changes and protected-status restrictions tested. |
| Maintenance release | Repaired copies released through POST; status and availability changes checked. |
| Withdrawal | Confirmation, cancellation, success feedback and retained withdrawn records verified. |
| Permanent deletion | Confirmation, cancellation, eligible deletions, success feedback and inventory count updates verified. |
| Validation presentation | Duplicate field-level messages removed from Physical Copy forms. |
| Responsive interface | Mobile inventory table scrolling and confirmation pages manually checked. |
| Design review | Local Dark Reader overrides identified and disabled; underlying green and ivory palette confirmed. |
| Historical deletion protection | Service tests cover historical references; no matching loan records found during manual SQL inspection. |
| Local build | Full Maven verification reported passing after the final Delete styling correction. |
| Git and CI | Completed Physical Copy management committed and pushed; GitHub Actions passed as reported by the developer. |

### M2 — Remaining persistence verification, 2026-11-13 to 2026-11-14

| Area | Verified result |
|---|---|
| User and Role persistence | Eight PostgreSQL integration tests passed. |
| User role associations | Multiple role assignments and association removal tested. |
| User email uniqueness | Case-insensitive lookup and duplicate rejection tested. |
| User role fetching | Entity-graph retrieval tested. |
| Circulation persistence | Nine PostgreSQL integration tests passed. |
| Reservation persistence | FIFO ordering and duplicate active reservation rejection tested. |
| Loan persistence | Active-loan uniqueness and optimistic-lock behavior tested. |
| Fine persistence | Daily accrual persistence and duplicate date rejection tested. |
| Purchase Suggestion persistence | Member associations, review information and status queries tested. |
| AI Result persistence | Pending results, multiple analysis records and processing-status queries tested. |
| JSONB mapping | Structured category and duplicate-candidate data saved and retrieved from PostgreSQL. |
| Timestamp precision | Review timestamp test corrected to use PostgreSQL microsecond precision. |
| Full Maven verification | 158 tests passed with `./mvnw clean verify`, as reported by the developer. |
| Git | Commits `9838fdd`, `54775c2` and `507ec60` pushed to `main`. |
| Continuous integration | GitHub Actions passed after the latest persistence commit, as reported by the developer. |
| Database migrations | No new migrations required for Steps 19.3A–19.3C. |

## Remaining work

The database migrations, permanent reference data, JPA mappings, repositories, Book Title management screens, Book Copy service operations, physical-copy management screens, integration tests and GitHub Actions CI workflow are in place.

All 12 application tables have the required JPA persistence mappings, including the `UserRoles` join table represented through the existing many-to-many relationship. The remaining persistence work concerns future application features rather than unmapped tables.

The final M2 review must confirm the professor's full parent-child CRUD requirement, particularly the Book Title deletion or deactivation workflow exposed through the web interface.

Circulation services, security, REST API, Gemini integration, scheduling, internationalization and deployment remain pending.

Additional test categories, concurrency verification, service coverage and performance measurements are also required. No coverage percentages, AI evaluation scores or production deployment results have been claimed.

The M2 Figma work includes reusable components, desktop and mobile screens, a clickable prototype, contrast measurements, design rationale and PDF export. The implemented application's accessibility and functional behavior must be tested separately from the prototype.

The next checkpoint is to complete the M2 requirements review, address any missing functionality and synchronize the remaining project documentation before moving into M3.