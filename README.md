# P03 Library Lending System

[![Java CI](https://github.com/LeoBrcina/library-lending-system/actions/workflows/ci.yml/badge.svg)](https://github.com/LeoBrcina/library-lending-system/actions/workflows/ci.yml)

**Course:** Rapid Development of Java Applications Using Programming Frameworks  
**Academic year:** 2026/27  
**University:** Algebra Bernays University  
**Project:** P03 - Library Lending System

## 1. Project Overview

The Library Lending System is a Spring Boot web application being developed to manage a public library's book catalog, physical copies, members, loans, reservations, overdue fines and purchase suggestions.

The application follows a hybrid lending workflow: members will be able to browse the catalog, manage eligible renewals, request reservations and submit purchase suggestions, while librarians will handle physical checkout and return.

Development follows the course milestones M1-M4 and the final submission requirements.

**Current status:** M1 foundation and design completed. M2 persistence, catalog management, physical-copy management and high-fidelity Figma design have been implemented. The final M2 requirements review is pending.

The application currently includes PostgreSQL persistence managed through Flyway, catalog services, Book Title management, Physical Copy management, user and role mappings, circulation persistence mappings and purchase-suggestion persistence. The application uses Spring MVC and Thymeleaf for the implemented web interfaces.

The repository includes automated tests covering domain validation, service operations, MVC workflows and PostgreSQL persistence. The latest locally verified build completed successfully with 158 passing tests. GitHub Actions builds and tests the project automatically.

The M2 Figma work includes reusable components, high-fidelity desktop and mobile screens, a tested clickable prototype and an accessibility design review.

Authentication, circulation services, REST endpoints, live AI integration, internationalization, scheduled jobs and public deployment remain future work.

## 2. Technology Stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.2 |
| Web layer | Spring MVC |
| Server-side templates | Thymeleaf |
| Persistence | Spring Data JPA / Hibernate 7 |
| Database | PostgreSQL 17 |
| Database migrations | Flyway |
| Connection pooling | HikariCP |
| Security | Spring Security 7 (planned) |
| Build tool | Maven Wrapper 3.10.0 |
| Testing | JUnit 6 and Spring Boot testing |
| Continuous integration | GitHub Actions |
| Local development | Docker Compose, IntelliJ IDEA |
| AI provider | Google Gemini API (selected, not integrated) |
| Planned hosting | Render Free |
| Planned managed database | Neon Free |
| UI/UX design | Figma |

## 3. Application Features

The following sections distinguish implemented functionality from planned application features.

### Library Catalog

Implemented:

- Browse the library catalog.
- Search, filter, sort and paginate book titles.
- View book details, authors, categories and physical-copy information.
- Create and edit book titles.
- Assign categories and select a primary category.
- Validate book title forms and display appropriate errors.
- Display empty catalog results.
- Manage individual physical copies through librarian-facing screens.
- Create, edit, withdraw and delete eligible physical copies.
- Release eligible copies from maintenance.
- Display physical-copy conditions, circulation statuses and inventory counts.
- Provide responsive catalog and physical-copy management interfaces.

The catalog and physical-copy workflows use Spring MVC controllers, service classes, DTOs, form objects and Spring Data JPA repositories.

Book Title deactivation exists at the service level. Its exposure through the web interface and the complete parent-child CRUD requirement will be reviewed during the final M2 audit.

### Member Features

Planned:

- View active and historical loans.
- Request eligible loan renewals.
- View overdue fines.
- Reserve titles when no borrowable copies are available.
- Track reservations and pickup deadlines.
- Submit book purchase suggestions.
- View purchase suggestion history and review decisions.

The database mappings and repositories supporting users, loans, reservations, fines and purchase suggestions have been implemented. Member-facing application services and interfaces remain pending.

### Librarian Features

Implemented:

- Manage book titles and their categories.
- Manage physical-copy inventory.
- Create and edit eligible physical copies.
- Withdraw or delete eligible copies.
- Release repaired copies from maintenance.
- View physical-copy conditions and circulation statuses.

Planned:

- Perform physical checkout and return.
- Manage reservations and copy allocation.
- Process loan renewals.
- Review purchase suggestions.
- Confirm or override AI-assisted acquisition recommendations.

The existing librarian-oriented catalog interfaces are not yet protected by role-based authentication.

### Administrator Features

Planned:

- Manage user accounts.
- Assign and manage application roles.
- Access administrator-specific functionality.

The `AppUser` and `Role` entities, repositories and role reference data have been implemented. Authentication, authorization and administrative screens remain pending.

### AI-Assisted Purchase Suggestions

The application will use Google Gemini to help librarians review member purchase suggestions.

The planned AI workflow will:

1. Analyze the submitted suggestion.
2. Recommend subject categories from the library's existing categories.
3. Identify potentially duplicate titles from relevant catalog candidates.
4. Present the results to a librarian.
5. Allow the librarian to confirm or override recommendations and make the final decision.

AI recommendations will be advisory. Purchase decisions will remain under human control.

The persistence layer currently includes `PurchaseSuggestion` and `AiSuggestionResult` entities with their repositories and integration tests.

AI recommendations are stored separately from the librarian's review information. Multiple analysis results can be associated with the same purchase suggestion.

The `AiSuggestionResult` entity uses Hibernate JSON mapping for PostgreSQL JSONB columns containing category recommendations and potential duplicate candidates.

The Figma prototype illustrates the intended review interface using sample data.

No live Gemini integration, AI processing service or AI evaluation has been completed.

## 4. Key Business Rules

The current project design includes the following policies:

| Rule | Value |
|---|---|
| Standard loan period | 21 days |
| Maximum active loans per member | 5 |
| Renewal extension | 14 days |
| Maximum renewals per loan | 2 |
| Overdue renewal | Not permitted |
| Renewal when another member has reserved the title | Not permitted |
| Overdue fine | €0.20 per overdue calendar day per loan |
| Reservation eligibility | Only when no borrowable copies exist |
| Reservation order | First-in-first-out (FIFO) |
| Reservation pickup deadline | 3 days |

Outstanding unpaid fines do not independently prevent borrowing.

Physical-copy management follows additional rules:

- Physical condition and circulation status are separate properties.
- Accepted conditions are `NEW`, `GOOD`, `FAIR`, `POOR` and `DAMAGED`.
- Accepted statuses are `AVAILABLE`, `ON_LOAN`, `ON_HOLD`, `MAINTENANCE` and `WITHDRAWN`.
- Copies in `POOR` or `DAMAGED` condition must be placed in maintenance.
- Inventory metadata can be edited only when a copy is available or in maintenance.
- Withdrawal preserves the physical-copy record and its history.
- Permanent deletion is prohibited when relevant loan or reservation-allocation history exists.
- Releasing a copy from maintenance requires an active title, acceptable condition and no conflicting circulation or reservation state.
- Acquisition dates are optional but cannot be in the future.

Physical-copy administrative operations use service-level validation, database integrity constraints and appropriate locking.

These policies are documented in greater detail in [docs/decisions.md](docs/decisions.md) and [ADR-001](docs/adr/001-persistence-model.md).

**Implementation note:** Physical-copy administration rules have been implemented and tested. Loan renewals, reservation allocation, overdue fine calculation and other circulation policies remain planned business rules until their services are implemented and verified.

## 5. M1 Deliverables

The following materials were prepared for the first milestone.

| Deliverable | Status | Location |
|---|---|---|
| Project topic selection | Confirmed | P03 Library Lending System |
| Domain ER diagram | Completed | [docs/diagrams](docs/diagrams) |
| Architecture Decision Record | Completed | [ADR-001](docs/adr/001-persistence-model.md) |
| Initial Spring Boot application | Generated and started locally | [src/main](src/main) |
| PostgreSQL database configuration | Created and verified locally | [compose.yaml](compose.yaml) |
| Low-fidelity Figma wireframes | Generated and reviewed | [docs/wireframes-m1](docs/wireframes-m1) |
| AI provider selection | Google Gemini | [docs/decisions.md](docs/decisions.md) |
| Hosting provider selection | Render Free + Neon Free | [docs/decisions.md](docs/decisions.md) |
| AI usage documentation | Maintained | [docs/ai-usage.md](docs/ai-usage.md) |

### Domain Model

The initial ER diagram defines 12 application tables organized around:

- Book titles, physical copies and categories.
- Users and roles.
- Loans, reservations and fine accruals.
- Member purchase suggestions and AI analysis results.

The diagram includes a parent-child relationship between book titles and physical copies, an attributed many-to-many relationship for categories and optional relationships for reservation allocation.

The ER diagram is available in both editable draw.io and PDF formats.

### M1 Low-Fidelity Figma Wireframes

The low-fidelity wireframes contain 11 main screen layouts and three state variations.

The screens cover public and member workflows, librarian operations, administrator user management and application feedback states.

**Figma design file:**

[View P03 Library Lending System UI/UX Prototype](https://www.figma.com/design/PiQSnO17vQGKHeqz3ktlb1/P03-Library-Lending-System---UI-UX-Prototype)

**M1 wireframe PDF exports:**

- [Public and Member](docs/wireframes-m1/A%20-%20Public%20and%20Member.pdf)
- [Librarian](docs/wireframes-m1/B%20-%20Librarian.pdf)
- [Administrator](docs/wireframes-m1/C%20-%20Administrator.pdf)
- [State Variations](docs/wireframes-m1/D%20-%20State%20Variations.pdf)

The grayscale M1 wireframes are preserved separately from the M2 high-fidelity designs.

## 6. M2 Persistence and Catalog Implementation

### Database Schema and Migrations

PostgreSQL 17 is used as the application's relational database. Flyway manages versioned schema changes and reference data.

The following migrations have been implemented and applied successfully:

| Migration | Purpose |
|---|---|
| `V1__create_library_schema.sql` | Creates the 12 application tables, relationships, constraints and initial indexes |
| `V2__strengthen_reservation_constraints.sql` | Strengthens reservation consistency, copy allocation and loan-return integrity |
| `V3__seed_reference_data.sql` | Seeds three application roles and ten library categories |

Hibernate is configured with `ddl-auto=validate`, meaning Flyway owns schema creation while Hibernate validates the JPA mappings.

Applied Flyway migrations are not modified. Subsequent database changes will use new migrations.

No additional migration was necessary for the M2 entity and repository implementations.

### Implemented JPA Entities

The persistence layer includes:

**Catalog:**

- `BookTitle`
- `BookCopy`
- `Category`
- `BookCategory`
- `BookCategoryId`

**Users and roles:**

- `AppUser`
- `Role`

**Circulation:**

- `Reservation`
- `ReservationStatus`
- `Loan`
- `FineAccrual`

**Purchase suggestions:**

- `PurchaseSuggestion`
- `PurchaseSuggestionStatus`
- `AiSuggestionResult`
- `AiProcessingStatus`

Additional domain enums include `BookCopyCondition` and `BookCopyStatus`.

The mappings demonstrate:

- A one-to-many relationship between book titles and physical copies.
- An attributed many-to-many relationship between book titles and categories.
- A many-to-many relationship between users and roles.
- Optional relationships for reservation allocation, loan fulfillment and librarian review.
- Relationships between loans and fine accruals.
- Relationships between purchase suggestions and AI analysis results.

`BookCategoryId` provides the composite identifier for the attributed book-category association.

User-role assignments use the existing `UserRoles` join table without a separate association entity.

The database schema also provides constraints for active loans, active reservations, reservation allocations, fine accrual uniqueness and other integrity requirements.

Actual circulation transitions and scheduled processing remain future service-layer work.

### Spring Data Repositories

The implemented repositories are:

**Catalog:**

- `BookTitleRepository`
- `BookCopyRepository`
- `CategoryRepository`
- `BookCategoryRepository`

**Users and roles:**

- `AppUserRepository`
- `RoleRepository`

**Circulation:**

- `ReservationRepository`
- `LoanRepository`
- `FineAccrualRepository`

**Purchase suggestions:**

- `PurchaseSuggestionRepository`
- `AiSuggestionResultRepository`

Repositories provide the required persistence operations and selected queries for catalog searching, role lookup, member records, reservation ordering, active loans, fine accruals and purchase suggestion history.

An entity graph is used for retrieving application users together with their assigned roles.

The required measured N+1 demonstration and SQL comparison remain pending.

### Book Title Management

The application includes Spring MVC and Thymeleaf workflows for:

- Listing book titles.
- Searching, filtering, sorting and pagination.
- Creating book titles.
- Viewing book title details.
- Editing book title information.
- Assigning and updating categories.
- Selecting the primary category.
- Handling invalid form submissions.
- Handling missing book titles.
- Displaying flash messages after successful operations.

The implementation uses:

- `BookCatalogQueryService`
- `BookTitleService`
- `BookTitleServiceImpl`
- `CategoryQueryService`
- `BookCatalogController`
- `BookTitleManagementController`

Form objects and service DTOs separate the web layer from JPA entities.

Book Title deactivation is implemented in the service layer. The complete parent CRUD screen requirement, including the appropriate deletion or deactivation workflow, requires a final M2 review.

### Physical Copy Management

The application includes physical-copy administration through the Book Title Details interface.

Implemented operations include:

- Add Physical Copy.
- Edit Physical Copy.
- Withdraw Physical Copy.
- Delete eligible Physical Copy.
- Release Physical Copy from Maintenance.

The implementation includes:

- `BookCopyService`
- `BookCopyServiceImpl`
- `BookCopyManagementController`
- `BookCopyForm`
- `BookCopyCommand`
- `BookCopyDetails`

The service layer enforces inventory validation, permitted administrative operations, historical-reference restrictions and maintenance-release conditions.

Physical-copy modifications use pessimistic locking where appropriate.

Withdrawal and deletion use separate confirmation pages. State-changing operations use POST requests.

Physical-copy inventory is displayed in a table with horizontal scrolling on narrow screens.

The user interface was manually reviewed for creation, editing, release, withdrawal, deletion, validation, protected statuses and responsive behavior.

### Persistence and Application Tests

The project contains automated tests covering:

- Catalog entity relationships and database constraints.
- Book Title services and MVC workflows.
- Physical-copy validation.
- Physical-copy creation and retrieval.
- Physical-copy metadata updates.
- Withdrawal and deletion restrictions.
- Maintenance release.
- Acquisition-date validation.
- User and role persistence.
- Circulation persistence and constraints.
- Purchase suggestion persistence.
- AI analysis result history and PostgreSQL JSONB mapping.

The circulation persistence tests include duplicate active-loan rejection, reservation ordering, fine accrual uniqueness and optimistic-lock behavior.

The purchase suggestion tests verify member ownership, librarian review information, status queries, multiple AI results and JSONB serialization and retrieval.

**Latest local verification:** `./mvnw clean verify` completed successfully with 158 passing tests.

These tests establish the functionality currently implemented. They do not establish that the planned circulation, security or live AI workflows are complete.

Further testing required by the professor includes repository slice tests, Testcontainers integration tests, deterministic AI stub tests, service-package coverage measurement and concurrency verification.

## 7. M2 High-Fidelity Figma Prototype

The M2 UI/UX work builds on the preserved M1 wireframes and provides a visual foundation for the Spring MVC and Thymeleaf application.

The Figma file contains three main pages:

1. `01 - Low-Fidelity Wireframes`
2. `02 - Components`
3. `03 - High-Fidelity Screens`

The high-fidelity designs use forest green (`#163A39`), warm ivory (`#F6F5F1`), muted gold (`#C78E52`), Inter typography, Auto Layout and reusable Figma components.

### Reusable Component Library

Eight native reusable component families were created:

- Button
- Text Input
- Select
- Status Badge
- Alert
- Navigation Item
- Catalog Table Row
- Category Badge

The component library contains 36 component variants. The components use consistent visual states, spacing and typography.

### High-Fidelity Screens

Six desktop frames at 1440px width were prepared and inspected:

| Figma frame | Purpose |
|---|---|
| Book Catalog | Search, filtering and management of bibliographic titles |
| Edit Book Title | Editing title information and categories |
| Book Title Details & Physical Copies | Displaying title information and managing physical copies |
| Book Catalog — Empty Results | Showing an unsuccessful catalog search |
| Edit Book Title — Validation Errors | Demonstrating required-field validation and preserved form values |
| AI-Assisted Purchase Suggestion Review | Reviewing AI category suggestions and possible duplicates before a human decision |

Two responsive frames at 390px width were also prepared:

- Book Catalog — Mobile 390
- Edit Book Title — Mobile 390

The mobile catalog replaces the desktop table with stacked book cards. The mobile edit form uses a single-column layout while retaining the original fields and classification controls.

### Clickable Prototype

A native Figma prototype demonstrates the librarian's catalog-management workflow:

1. Open the Book Catalog.
2. Select View for a book title.
3. Open the title details and select Edit Title.
4. Use Save to return to the title details with success feedback.
5. Return to the catalog using the breadcrumb.

Cancel navigation was also connected. The developer tested the main navigation flow successfully in Figma presentation mode.

The prototype demonstrates intended navigation rather than working backend functionality. Form submissions inside Figma do not persist records.

The corresponding catalog and physical-copy workflows have since been implemented separately in the Spring Boot application.

### Thymeleaf Template Mapping

The following table maps the approved Figma designs to the current or planned Thymeleaf implementation.

| Figma frame | Thymeleaf implementation | Status |
|---|---|---|
| Book Catalog | `templates/catalog/list.html` | Implemented |
| Edit Book Title | `templates/catalog/edit.html` | Implemented |
| Book Title Details & Physical Copies | `templates/catalog/details.html` | Implemented |
| Book Catalog — Empty Results | `templates/catalog/list.html` | Implemented |
| Edit Book Title — Validation Errors | `templates/catalog/edit.html` | Implemented |
| AI-Assisted Purchase Suggestion Review | Acquisitions review template | Planned |
| Book Catalog — Mobile 390 | Responsive catalog layout | Implemented |
| Edit Book Title — Mobile 390 | Responsive Book Title form | Implemented |

Additional Physical Copy templates include:

- `templates/catalog/copies/create.html`
- `templates/catalog/copies/edit.html`
- `templates/catalog/copies/withdraw.html`
- `templates/catalog/copies/delete.html`

The actual application uses a shared catalog workspace and reusable layout fragments.

The implementation follows the approved visual design while adapting certain interactions to the requirements of the working application.

The Physical Copy inventory retains a horizontally scrollable table on mobile instead of introducing a separate card layout.

### Accessibility Review

A read-only inspection of the desktop and mobile Figma screens extracted foreground and background colors for numerical WCAG contrast analysis.

All 20 distinct text/background combinations identified by that inspection met the WCAG AA minimum contrast ratio of 4.5:1 for normal text.

Representative results include:

| Combination | Contrast ratio |
|---|---:|
| Primary text on white | 13.20:1 |
| Secondary text on white | 5.79:1 |
| Secondary text on ivory | 5.30:1 |
| White text on forest green | 12.36:1 |
| Error text on pale red | 6.55:1 |
| Input border on white | 3.64:1 |
| Focus outline on white | 4.77:1 |

Some light decorative colors fall below the 3:1 non-text contrast target and must not be the sole meaningful control or status indicators.

The review does not establish full WCAG compliance. Keyboard navigation, screen-reader behavior, browser-rendered contrast and other accessibility requirements must be verified after implementation.

### Figma Deliverables

- [Figma design and prototype file](https://www.figma.com/design/PiQSnO17vQGKHeqz3ktlb1/P03-Library-Lending-System---UI-UX-Prototype)
- [High-fidelity prototype PDF](docs/prototype.pdf)
- [UI/UX design rationale and accessibility review](docs/design-rationale.md)
- [AI usage and verification log](docs/ai-usage.md)

The developer exported an eight-page high-fidelity prototype PDF and visually checked the two mobile frames on its final pages.

The Figma link is recorded, but anonymous or public viewing permissions still require verification.

## 8. Local Development Setup

### Prerequisites

- JDK 25
- Docker Desktop with Docker Compose
- Git

The repository includes Maven Wrapper, so a separate Maven installation is not required.

### Start PostgreSQL

Create a local `.env` file using `.env.example` as a reference.

Configure the PostgreSQL database name, username and a local password.

The `.env` file is ignored by Git and must not be committed.

Start PostgreSQL:

```bash
docker compose up -d
```

Check the database container:

```bash
docker compose ps
```

The development configuration exposes PostgreSQL on:

```text
127.0.0.1:5433
```

PostgreSQL runs on its standard port 5432 inside the Docker container. Port 5433 is used for the Windows host connection.

### Configure the Application

The Spring Boot application reads its database password from the `DB_PASSWORD` environment variable.

The local datasource defaults to:

```text
jdbc:postgresql://localhost:5433/library_lending
```

The default database username is `library_app`.

Set `DB_PASSWORD` in your local environment or in a local IntelliJ IDEA Run Configuration before starting the application.

Do not commit passwords, API keys or other credentials.

### Build and Test

From the repository root, run:

```bash
./mvnw clean verify
```

The current test suite contains 158 tests, including domain validation tests, service tests, MVC tests and PostgreSQL integration tests.

The integration tests require a running PostgreSQL database and a valid `DB_PASSWORD` environment variable.

The full suite has passed locally through Maven.

### Run the Application

Start the application using:

```bash
./mvnw spring-boot:run
```

Alternatively, run `LibraryLendingSystemApplication` from IntelliJ IDEA.

The application uses port `8080`.

The catalog is available at:

```text
http://localhost:8080/catalog
```

The current application supports catalog browsing and Book Title and Physical Copy management.

Authentication and role-based access restrictions have not yet been implemented.

## 9. Continuous Integration

GitHub Actions automatically builds and tests the project on every push and pull request.

Workflow file:

[`.github/workflows/ci.yml`](.github/workflows/ci.yml)

The workflow:

1. Checks out the repository.
2. Configures Eclipse Temurin Java 25.
3. Starts a PostgreSQL 17 service container.
4. Supplies temporary CI-only database connection settings.
5. Runs Maven verification using the project Maven Wrapper.
6. Reports the build and test results in GitHub Actions.

The PostgreSQL service uses port 5432 in the GitHub Actions environment. The application datasource URL is overridden through `DB_URL`, so the workflow does not require changes to the local port 5433 configuration.

**Latest verified checkpoint:** The developer reported a successful GitHub Actions run after the purchase suggestion and AI result persistence implementation was pushed in commit `507ec60`.

The associated local Maven verification completed with 158 passing tests.

The current workflow performs continuous integration only. Automatic production deployment is planned for a later milestone.

[View GitHub Actions runs](https://github.com/LeoBrcina/library-lending-system/actions)

## 10. Project Structure

```text
library-lending-system/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .mvn/
├── docs/
│   ├── adr/
│   │   └── 001-persistence-model.md
│   ├── diagrams/
│   │   └── LibraryLendingSystem_ERD.drawio
│   ├── wireframes-m1/
│   │   ├── A - Public and Member.pdf
│   │   ├── B - Librarian.pdf
│   │   ├── C - Administrator.pdf
│   │   └── D - State Variations.pdf
│   ├── ai-usage.md
│   ├── decisions.md
│   ├── design-rationale.md
│   └── prototype.pdf
├── src/
│   ├── main/
│   │   ├── java/hr/algebra/librarylendingsystem/
│   │   │   ├── domain/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── web/
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── static/css/
│   │       ├── templates/
│   │       │   ├── catalog/
│   │       │   │   └── copies/
│   │       │   └── fragments/
│   │       └── application.properties
│   └── test/
│       └── java/hr/algebra/librarylendingsystem/
├── .env.example
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

The structure contains the implemented M2 persistence and web application packages.

Additional security, REST, AI integration, scheduling and deployment components will be added during subsequent milestones.

## 11. Development Roadmap

### M1 — Foundation and Design

- Define the public library domain and business rules.
- Prepare the ER diagram and first ADR.
- Initialize the Spring Boot application.
- Configure local PostgreSQL through Docker.
- Prepare low-fidelity Figma wireframes.
- Select AI and hosting providers.

**Status:** Completed as the initial design and technical foundation.

### M2 — Persistence and Catalog CRUD

Implemented:

- Flyway V1-V3 database migrations and reference data.
- Catalog JPA entities and repositories.
- User and role persistence.
- Loan, reservation and fine persistence mappings.
- Purchase suggestion and AI analysis result persistence.
- Catalog browsing, searching, filtering, sorting and pagination.
- Book Title creation, details and editing.
- Physical Copy creation, editing, maintenance release, withdrawal and deletion.
- Domain validation and service-layer business rules for physical-copy administration.
- MVC and PostgreSQL integration tests.
- GitHub Actions build-and-test workflow.
- High-fidelity Figma screens and reusable components.
- Responsive desktop and mobile design variants.
- Clickable librarian catalog-management prototype.
- Design contrast review and rationale.
- Eight-page prototype PDF export.

Remaining M2 work:

- Review complete parent-child CRUD coverage against the professor's specification.
- Address any missing Book Title management operations or web workflows.
- Verify remaining M2 deliverables and implementation evidence.
- Synchronize project documentation with the verified implementation.

**Status:** Implementation substantially complete. Final requirements review pending.

### M3 — Security, REST API and AI

Planned:

- Spring Security 7 with three roles and service-level authorization.
- Form-based authentication, BCrypt and CSRF protection.
- Protected member, librarian and administrator operations.
- REST API for the Book Title and Physical Copy aggregate.
- DTOs, validation, pagination, sorting and ProblemDetail responses.
- OpenAPI documentation.
- AI provider abstraction and deterministic test stub.
- Initial Google Gemini integration.
- AI-assisted purchase suggestion review.
- Initial AI evaluation cases.

Circulation services and member workflows will also be scheduled according to their dependencies.

### M4 — Testing, Scheduling and Deployment

Planned:

- Complete remaining circulation, reservation and fine-processing workflows.
- Additional service unit tests, MVC slice tests and repository slice tests.
- Testcontainers integration tests.
- JaCoCo verification of at least 80% service-package line coverage.
- Croatian and English internationalization.
- Two scheduled jobs with safe execution and recovery behavior.
- Persistence performance analysis and measured optimization.
- Docker application packaging and full application/database Compose configuration.
- Public HTTPS deployment with a managed PostgreSQL database.

### Final Submission

Planned:

- Complete documentation and three ADRs.
- AI evaluation documentation with at least ten real test cases.
- Final review of the Figma prototype PDF and design rationale.
- Coverage report.
- Measured performance improvement evidence.
- Working deployment and demo accounts.
- Ten-minute application recording.
- Defense preparation.

## 12. Architecture and AI Documentation

Key development documents:

- [Project Decision Register](docs/decisions.md)
- [ADR-001: Persistence Model and Transactional Consistency](docs/adr/001-persistence-model.md)
- [AI Usage Log](docs/ai-usage.md)
- [UI/UX Design Rationale](docs/design-rationale.md)

The project uses AI assistance for requirements analysis, architectural planning, documentation, persistence implementation guidance, testing and UI design.

AI-generated material is reviewed by the developer. Implementation details, test results and milestone evidence are documented only when actually verified.

ADR-002, ADR-003 and the required AI evaluation document will be prepared during their corresponding development phases.

ADR-002 will address AI integration, structured output and human review.

ADR-003 will address reliable scheduled processing, locking and recovery behavior.

## 13. Current Project Status

**Milestone:** M2 implementation substantially complete; final compliance review pending.

**Implemented and verified:**

- Java 25, Spring Boot 4.1.2 and Maven Wrapper.
- PostgreSQL 17 through Docker Compose.
- Spring Boot datasource configuration and startup.
- Twelve application tables through Flyway.
- Three database migrations and permanent reference data.
- Catalog JPA entities and repositories.
- User and role persistence mappings and repositories.
- Loan, reservation and fine persistence mappings and repositories.
- Purchase suggestion and AI analysis result persistence.
- PostgreSQL JSONB mapping for AI recommendations.
- Catalog query functionality with search, filtering, sorting and pagination.
- Book Title creation, details and editing.
- Physical Copy creation, editing, withdrawal, safe deletion and maintenance release.
- Service-level validation and locking for physical-copy administration.
- Spring MVC and Thymeleaf catalog management interfaces.
- Responsive catalog and physical-copy management layouts.
- 158 passing automated tests through local Maven verification.
- GitHub Actions CI, including a successful run reported after commit `507ec60`.

**Prepared and reviewed:**

- Domain ER diagram.
- Persistence ADR.
- Project decision register.
- Low-fidelity Figma wireframes.
- Eight high-fidelity Figma screens, including two mobile variants.
- Reusable component library and clickable prototype.
- Numerical contrast audit and design rationale.
- Eight-page prototype PDF export.
- AI provider and deployment decisions.

**Remaining M2 review:**

- Complete parent-child CRUD requirements.
- Book Title deactivation or deletion interface.
- Final milestone compliance verification.
- Documentation synchronization.

**Not yet implemented:**

- Authentication and authorization.
- REST API and OpenAPI.
- Member dashboard and account management workflows.
- Librarian checkout and return.
- Loan renewal services.
- Reservation allocation and expiration.
- Overdue fine calculation and reconciliation.
- Purchase suggestion submission and review services.
- Live Google Gemini integration and evaluation.
- Scheduled processing.
- Internationalization.
- Application containerization and production deployment.
- Required Testcontainers tests, coverage reports and performance evidence.

The project is developed incrementally. Implemented persistence mappings are not treated as completed business workflows, and prepared designs are not presented as verified application functionality.