
# P03 Library Lending System

**Course:** Rapid Development of Java Applications Using Programming Frameworks  
**Academic year:** 2026/27  
**University:** Algebra Bernays University  
**Project:** P03 - Library Lending System

## 1. Project Overview

The Library Lending System is a Spring Boot web application designed to manage a public library's book catalog, physical copies, members, loans, reservations, overdue fines and purchase suggestions.

The application follows a hybrid lending workflow: members can browse the catalog, manage eligible renewals, request reservations and submit purchase suggestions, while librarians handle physical book checkout and return.

The project is developed incrementally according to the course milestones M1-M4 and the final submission requirements.

**Current status:** M1 preparation completed. The application foundation, database configuration, domain model, architecture decisions and low-fidelity UI wireframes have been prepared. Business functionality has not yet been implemented.

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
| Security | Spring Security (planned) |
| Build tool | Maven Wrapper |
| Local development | Docker Compose, IntelliJ IDEA |
| AI provider | Google Gemini API (selected, not integrated) |
| Planned hosting | Render Free |
| Planned managed database | Neon Free |
| UI/UX design | Figma |

## 3. Planned Application Features

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

The following materials have been prepared for the first milestone.

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

### Figma Prototype

The low-fidelity wireframes contain 11 main screen layouts and three state variations.

The screens cover public and member workflows, librarian operations, administrator user management and application feedback states.

**Figma design file:**

[View P03 Library Lending System UI/UX Prototype](https://www.figma.com/design/PiQSnO17vQGKHeqz3ktlb1/P03-Library-Lending-System---UI-UX-Prototype)

**M1 wireframe PDF exports:**

- [Public and Member](docs/wireframes-m1/A%20-%20Public%20and%20Member.pdf)
- [Librarian](docs/wireframes-m1/B%20-%20Librarian.pdf)
- [Administrator](docs/wireframes-m1/C%20-%20Administrator.pdf)
- [State Variations](docs/wireframes-m1/D%20-%20State%20Variations.pdf)

The grayscale wireframes are preserved separately from the planned high-fidelity screens.

## 6. Local Development Setup

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

### Configure the Application

The Spring Boot application reads its database password from the `DB_PASSWORD` environment variable.

The local datasource defaults to:

```text
jdbc:postgresql://localhost:5433/library_lending
```

The default database username is `library_app`.

Set `DB_PASSWORD` in your local environment or in a local IntelliJ IDEA Run Configuration before starting the application.

Do not commit passwords, API keys or other credentials.

### Build and Run

From the repository root:

```bash
./mvnw -DskipTests package
```

Start the application:

```bash
./mvnw spring-boot:run
```

Alternatively, run `LibraryLendingSystemApplication` from IntelliJ IDEA.

The application uses port `8080`.

**Current limitation:** No web controllers or Thymeleaf pages have been implemented yet. An HTTP 404 response at `http://localhost:8080/` is expected at the current stage.

The initial Maven package build and Spring Boot connection to PostgreSQL were verified locally. Tests were skipped during the initial build and are not reported as passing.

## 7. Project Structure

```text
library-lending-system/
├── docs/
│   ├── adr/
│   │   └── 001-persistence-model.md
│   ├── diagrams/
│   │   ├── LibraryLendingSystem_ERD.drawio
│   │   └── LibraryLendingSystem_ERD.drawio.pdf
│   ├── wireframes-m1/
│   │   ├── A - Public and Member.pdf
│   │   ├── B - Librarian.pdf
│   │   ├── C - Administrator.pdf
│   │   └── D - State Variations.pdf
│   ├── ai-usage.md
│   └── decisions.md
├── src/
│   ├── main/
│   └── test/
├── .env.example
├── compose.yaml
├── pom.xml
├── mvnw
└── README.md
```

This structure represents the current M1 baseline. Additional application packages, database migrations, tests and deployment files will be added in later milestones.

## 8. Development Roadmap

### M1 - Foundation and Design

- Define the public library domain and business rules.
- Prepare the ER diagram and first ADR.
- Initialize the Spring Boot application.
- Configure local PostgreSQL through Docker.
- Prepare low-fidelity Figma wireframes.
- Select AI and hosting providers.

### M2-M4 - Incremental Implementation

Planned work includes:

- Flyway database schema and seed migrations.
- JPA entities, repositories and service layer.
- Spring MVC and Thymeleaf CRUD interfaces.
- Validation, error handling and role-based security.
- REST API endpoints.
- Member loans, renewals, reservations and fines.
- AI-assisted purchase-suggestion review.
- Automated tests and persistence performance analysis.
- High-fidelity Figma prototype and reusable components.
- Scheduled processing, internationalization and deployment.

The exact implementation order will follow the professor's milestone requirements.

## 9. Architecture and AI Documentation

Key development documents:

- [Project Decision Register](docs/decisions.md)
- [ADR-001: Persistence Model and Transactional Consistency](docs/adr/001-persistence-model.md)
- [AI Usage Log](docs/ai-usage.md)

The project uses AI assistance for requirements analysis, architectural planning, documentation and UI design.

AI-generated material is reviewed by the developer. Implementation details, test results and milestone evidence are documented only when actually verified.

## 10. Current Project Status

**Milestone:** M1 preparation completed.

**Verified locally:**

- Java 25 and Maven Wrapper configured.
- Spring Boot 4.1.2 project generated.
- Initial Maven package build completed with tests skipped.
- PostgreSQL 17 container started successfully.
- Spring Boot connected to PostgreSQL.
- Embedded web server started and responded to HTTP requests.

**Prepared and reviewed:**

- Domain ER diagram.
- Persistence ADR.
- Project decision register.
- Low-fidelity Figma wireframes.
- AI provider and deployment decisions.

**Not yet implemented:**

- Application database schema and seed migrations.
- Domain entities and repositories.
- Business services and CRUD functionality.
- Authentication and authorization.
- Member and librarian workflows.
- AI API integration.
- Scheduled fine accrual.
- Production deployment.

The project is being developed incrementally, with implementation and testing evidence added as each milestone progresses.