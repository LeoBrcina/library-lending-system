
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

**Current status:** M1 foundation and design completed. M2 implementation is in progress. The PostgreSQL schema, Flyway migrations, reference data, initial catalog JPA mappings, repositories and persistence integration tests have been implemented. GitHub Actions CI builds and tests the project automatically.

The M2 Figma design work is also prepared, including reusable components, high-fidelity desktop and mobile screens, a tested clickable prototype and an accessibility design review. The catalog business services and web interfaces have not yet been implemented.

## 2. Technology Stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.2 |
| Web layer | Spring MVC |
| Server-side templates | Thymeleaf |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 17 |
| Database migrations | Flyway |
| Security | Spring Security 7 (planned) |
| Build tool | Maven Wrapper 3.10.0 |
| Continuous integration | GitHub Actions |
| Local development | Docker Compose, IntelliJ IDEA |
| AI provider | Google Gemini API (selected, not integrated) |
| Planned hosting | Render Free |
| Planned managed database | Neon Free |
| UI/UX design | Figma |

## 3. Planned Application Features

The following features describe the intended application. Unless explicitly identified as implemented, they remain planned.

### Library Catalog

- Browse and search the library catalog.
- View book details, authors, categories and availability.
- Manage book titles and their individual physical copies.
- Maintain relationships between book titles and categories.

### Member Features

- View active and historical loans.
- Request eligible loan renewals.
- View overdue fines.
- Reserve titles when no borrowable copies are available.
- Track reservations and pickup deadlines.
- Submit book purchase suggestions.

### Librarian Features

- Manage book titles and physical copies.
- Perform physical checkout and return.
- Manage reservations and copy allocation.
- Review purchase suggestions.
- Confirm or override AI-assisted acquisition recommendations.

### Administrator Features

- Manage user accounts.
- Assign and manage application roles.

### AI-Assisted Purchase Suggestions

The application will use Google Gemini to help librarians review member purchase suggestions.

The planned AI workflow will:

1. Analyze the submitted suggestion.
2. Recommend subject categories from the library's existing categories.
3. Identify potentially duplicate titles from relevant catalog candidates.
4. Present the results to a librarian.
5. Allow the librarian to confirm or override recommendations and make the final decision.

AI recommendations will be advisory. Purchase decisions will remain under human control.

The Figma prototype illustrates the intended review interface using sample data. No live Gemini integration or AI evaluation has been completed.

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

These policies are documented in greater detail in [docs/decisions.md](docs/decisions.md) and [ADR-001](docs/adr/001-persistence-model.md).

**Implementation note:** These are accepted business rules, not claims that they have already been implemented or tested.

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

## 6. M2 Persistence Implementation

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

### Implemented JPA Entities

The following catalog persistence classes have been created:

- `BookTitle`
- `BookCopy`
- `Category`
- `BookCategory`
- `BookCategoryId`

The mappings demonstrate a one-to-many relationship between book titles and physical copies, and an attributed many-to-many relationship between book titles and categories through `BookCategory`.

`BookCategoryId` provides the composite identifier for the association entity.

The remaining application tables exist in PostgreSQL but have not yet been mapped to Java.

### Spring Data Repositories

Four repositories provide persistence access to the initial catalog model:

- `BookTitleRepository`
- `BookCopyRepository`
- `CategoryRepository`
- `BookCategoryRepository`

Repository integration tests verify persistence, retrieval, entity relationships and the database restriction against multiple primary categories for a title.

Catalog business services, DTOs, validation rules, controllers and Thymeleaf CRUD screens are the next implementation steps.

## 7. M2 High-Fidelity Figma Prototype

The M2 UI/UX work builds on the preserved M1 wireframes and provides a visual foundation for the planned Spring MVC and Thymeleaf application.

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

The prototype demonstrates intended navigation rather than working backend functionality. Form submissions do not persist records, and the application services have not yet been implemented.

### Planned Thymeleaf Template Mapping

The following mapping will guide the future Spring MVC implementation. These template files have not yet been created.

| Figma frame | Planned Thymeleaf template |
|---|---|
| Book Catalog | `templates/librarian/books/list.html` |
| Edit Book Title | `templates/librarian/books/form.html` |
| Book Title Details & Physical Copies | `templates/librarian/books/details.html` |
| Book Catalog — Empty Results | `templates/librarian/books/list.html` with an empty-result state |
| Edit Book Title — Validation Errors | `templates/librarian/books/form.html` with validation errors |
| AI-Assisted Purchase Suggestion Review | `templates/librarian/acquisitions/review.html` |
| Book Catalog — Mobile 390 | Responsive CSS for `templates/librarian/books/list.html` |
| Edit Book Title — Mobile 390 | Responsive CSS for `templates/librarian/books/form.html` |

The empty-results and validation-error designs represent states of existing templates, not separate pages. Mobile variants will use the same server-rendered templates with responsive styling.

The eventual implementation will separate controllers, services, repositories, DTOs and form objects. JPA entities will not be exposed directly to Thymeleaf forms.

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

The developer exported an eight-page high-fidelity prototype PDF and visually checked the two mobile frames on its final pages. The complete PDF file has not been independently inspected in this review.

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
./mvnw verify
```

The test suite currently contains four tests, including three catalog repository integration tests and one Spring Boot application context test.

These tests require a running PostgreSQL database and a valid `DB_PASSWORD` environment variable.

The full suite has passed locally through Maven and IntelliJ.

### Run the Application

Start the application using:

```bash
./mvnw spring-boot:run
```

Alternatively, run `LibraryLendingSystemApplication` from IntelliJ IDEA.

The application uses port `8080`.

**Current limitation:** No web controllers or Thymeleaf pages have been implemented yet. An HTTP 404 response at `http://localhost:8080/` is expected at the current stage.

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

**Verified result:** The initial GitHub Actions workflow completed successfully with all four tests passing.

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
│   │   │   └── repository/
│   │   └── resources/
│   │       ├── db/migration/
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

The structure includes the locally prepared M2 design documentation. Additional packages, mappings, templates, tests and deployment files will be added incrementally.

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

Completed or prepared:

- Flyway V1-V3 database migrations and reference data.
- Initial catalog JPA entities and repositories.
- Catalog persistence integration tests.
- GitHub Actions build-and-test workflow.
- High-fidelity Figma screens and reusable components.
- Responsive desktop and mobile design variants.
- Clickable librarian catalog-management prototype.
- Design contrast review and rationale.
- Eight-page prototype PDF export.

Next steps:

- Verify final Figma documentation, sharing permissions and repository changes.
- Complete catalog business services and parent-child CRUD screens.
- Complete the remaining JPA mappings and persistence tests.
- Prepare and review M2 implementation evidence.

### M3 — Security, REST API and AI

Planned:

- Spring Security with three roles and service-level authorization.
- Validation and protected web operations.
- Aggregate REST endpoints with DTOs, pagination, status codes and ProblemDetail.
- OpenAPI documentation.
- AI provider abstraction, deterministic stub and initial evaluation cases.
- AI-assisted purchase-suggestion review.

### M4 — Testing, Scheduling and Deployment

Planned:

- Additional JUnit and Mockito service tests, MVC tests and Testcontainers integration tests.
- JaCoCo verification of at least 80% service-package line coverage.
- Croatian and English internationalization.
- Two scheduled jobs with safe execution and recovery behavior.
- Persistence performance analysis and measured optimization.
- Docker application packaging and full application/database Compose configuration.
- Public HTTPS deployment with a managed PostgreSQL database.

### Final Submission

Planned:

- Complete documentation and three ADRs.
- AI evaluation documentation.
- Final review of the Figma prototype PDF and design rationale.
- Coverage report.
- Working deployment and demo accounts.
- Application recording and defense preparation.

## 12. Architecture and AI Documentation

Key development documents:

- [Project Decision Register](docs/decisions.md)
- [ADR-001: Persistence Model and Transactional Consistency](docs/adr/001-persistence-model.md)
- [AI Usage Log](docs/ai-usage.md)
- [UI/UX Design Rationale](docs/design-rationale.md)

The project uses AI assistance for requirements analysis, architectural planning, documentation, persistence implementation guidance, testing and UI design.

AI-generated material is reviewed by the developer. Implementation details, test results and milestone evidence are documented only when actually verified.

ADR-002, ADR-003 and the required AI evaluation document will be prepared during their corresponding development phases.

## 13. Current Project Status

**Milestone:** M2 implementation in progress.

**Implemented and verified:**

- Java 25, Spring Boot 4.1.2 and Maven Wrapper.
- PostgreSQL 17 through Docker Compose.
- Spring Boot datasource configuration and startup.
- Twelve application tables through Flyway.
- Three database migrations and permanent reference data.
- Initial catalog JPA entities and four repositories.
- Three catalog repository integration tests and the application context test.
- Four passing tests through IntelliJ and Maven.
- GitHub Actions CI with a successful four-test build.

**Prepared and reviewed:**

- Domain ER diagram.
- Persistence ADR.
- Project decision register.
- Low-fidelity Figma wireframes.
- Eight high-fidelity Figma screens, including two mobile variants.
- Reusable component library and clickable prototype.
- Numerical contrast audit and design rationale.
- Eight-page prototype PDF export, pending full-file inspection.
- AI provider and deployment decisions.

**Not yet implemented:**

- Remaining JPA entity mappings and repositories.
- Catalog business services and CRUD screens.
- Authentication and authorization.
- REST API and OpenAPI.
- Member and librarian circulation workflows.
- AI API integration and evaluation.
- Scheduled fine accrual and reservation expiry.
- Internationalization.
- Application containerization and production deployment.
- Final testing, coverage and performance evidence.

The project is developed incrementally. Prepared designs and planned features are not presented as verified application functionality.
