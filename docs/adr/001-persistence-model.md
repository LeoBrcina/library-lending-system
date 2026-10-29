# ADR-001: Persistence Model and Transactional Consistency

**Status:** Accepted (design stage)  
**Date:** 2026-10-28  
**Project:** P03 Library Lending System  
**Milestone:** M1

## 1. Context

The Library Lending System manages book titles, physical copies, members, loans, reservations, overdue fines and purchase suggestions.

The project requires a relational persistence model using Spring Data JPA and Hibernate 7. The database must support one-to-many relationships, a many-to-many relationship with an association entity carrying additional attributes and an optional relationship.

The main challenges are maintaining historical borrowing records, preventing conflicting transactions, supporting fair reservation allocation and ensuring that overdue fines cannot be charged twice when a scheduled job is repeated.

The persistence model must also support the required Thymeleaf CRUD screens, REST API, automated testing and future deployment.

## 2. Decision

### 2.1 Database technology

PostgreSQL will be used as the relational database.

Spring Data JPA with Hibernate 7 will provide object-relational mapping, while Flyway will manage versioned database schema migrations and seed data.

PostgreSQL will run in Docker during local development. The deployed application will use a managed PostgreSQL database.

The application will use HikariCP for database connection pooling. Pool configuration will be selected deliberately based on the deployment environment and available database connections.

The final database schema will be managed through Flyway migrations rather than Hibernate automatic schema generation.

### 2.2 Domain model

The database design contains 12 planned application tables:

- **Catalog:** BookTitles, BookCopies, Categories and BookCategories.
- **Users and security:** AppUsers, Roles and UserRoles.
- **Lending:** Loans, Reservations and FineAccruals.
- **AI acquisitions:** PurchaseSuggestions and AiSuggestionResults.

Book titles and physical copies are modeled separately because multiple physical copies may belong to the same bibliographic title.

Authors are stored as text in BookTitles to avoid unnecessary entities and relationships.

BookCategories represents the many-to-many relationship between titles and categories. Its IsPrimary attribute identifies a title's primary classification.

AppUsers stores account information and BCrypt password hashes. UserRoles allows an account to have multiple roles.

Loans preserve borrowing and return history. Reservations track waiting members and allocated copies. FineAccruals records individual overdue charges.

PurchaseSuggestions and AiSuggestionResults separate member requests, AI-generated recommendations and librarian decisions.

### 2.3 Naming conventions

Java entity classes use singular PascalCase names, such as BookTitle and FineAccrual.

Database tables use plural PascalCase logical names, such as BookTitles and FineAccruals. Database columns use PascalCase names, such as BookTitleId and AccrualDate.

Java fields and methods follow standard camelCase naming conventions.

PostgreSQL folds unquoted identifiers to lowercase. The implementation will use consistent SQL identifiers and explicit JPA mappings where necessary to prevent naming mismatches.

Quoted mixed-case identifiers will not be required unless a specific technical reason is identified.

### 2.4 JPA relationships and fetching strategy

The persistence model will demonstrate the relationship types required by the assignment:

- **One-to-many:** BookTitle to BookCopy.
- **Many-to-many with an association entity:** BookTitle to Category through BookCategory, which contains the IsPrimary attribute.
- **Optional relationship:** Reservation to an allocated BookCopy, which remains null while the reservation is waiting.

Relationships will use lazy fetching by default where appropriate to avoid loading unnecessary related entities.

For operations requiring related records, query-specific fetch joins or entity graphs will be used.

At least one N+1 query problem will be identified and measured during implementation. The original and optimized SQL queries will be documented, together with the observed improvement.

Entities will not be exposed directly through REST endpoints. DTOs and form objects will separate persistence concerns from the web and API layers.

### 2.5 Loan history and borrowing rules

Each Loan references one BookCopy and one borrowing member. The issuing librarian is recorded, and the returning librarian is recorded when applicable.

Completed loans remain stored as historical records.

The application will enforce the following policies:

- Standard loan duration: 21 days.
- Maximum five active loans per member.
- Maximum two renewals per loan.
- Each successful renewal extends the current due date by 14 days.
- Overdue loans cannot be renewed.
- Renewals are prohibited when another member holds an active reservation for the title.
- Outstanding fines do not independently prevent new borrowing.

Librarians and administrators may borrow books when granted member privileges, but their staff roles do not bypass borrowing rules.

Borrowing eligibility and renewal restrictions will be enforced in the service layer regardless of which authorized user initiates the operation.

### 2.6 Transactional consistency and database constraints

Checkout, return, renewal and reservation allocation will be implemented as transactional service operations.

The database will enforce important integrity constraints, including:

- Unique inventory codes for physical book copies.
- Unique user email addresses.
- Unique BookTitleId and CategoryId combinations in BookCategories.
- At most one primary category per title.
- At most one active loan per physical copy.
- A renewal count between zero and two.
- At most one active reservation per member and title.
- At most one active reservation allocation per physical copy.
- At most one loan associated with a fulfilled reservation.
- Unique LoanId and AccrualDate combinations in FineAccruals.

PostgreSQL partial unique indexes will be used where uniqueness depends on an active record state.

Business rules involving multiple records, such as the maximum number of active loans, will be validated in the service layer within appropriate transaction boundaries.

Database locking and concurrency controls will protect operations that could otherwise conflict.

Operations affecting the same title, member or physical copy will follow a consistent locking strategy to reduce race conditions and deadlock risks.

The exact locking queries, acquisition order and transaction isolation requirements will be finalized during implementation and verified using integration tests.

### 2.7 Reservations

Reservations belong to book titles rather than individual physical copies.

A member may create a reservation only when the title has no borrowable copies available.

Waiting reservations follow a deterministic first-in-first-out order using their request timestamp and identifier.

When a physical copy becomes available, the application automatically allocates it to the first eligible waiting reservation.

The member has three days to collect the allocated copy.

When a pickup deadline expires, the reservation is marked expired, the copy is released and the next eligible reservation is promoted.

A reservation may optionally reference an allocated physical copy. A loan may optionally reference the reservation it fulfilled.

A fulfilled reservation may be associated with at most one loan.

If an allocated copy becomes damaged or unavailable before collection, the affected reservation returns to the waiting state while retaining its original queue priority.

Reservation allocation, expiration and fulfillment will be coordinated transactionally to prevent double allocation and unfair queue advancement.

### 2.8 Overdue fines and idempotency

FineAccruals stores individual daily overdue charges associated with loans.

The agreed fine is €0.20 per overdue calendar day, calculated using the Europe/Zagreb time zone.

A book is due through the end of its specified due date. Each calendar date after the due date, up to and including the local return date when applicable, incurs a fine of €0.20.

For example, a book due on November 10 has no fine if returned that day. Returning it on November 11 incurs €0.20, while returning it on November 12 incurs €0.40.

The database will enforce a unique constraint on the combination of LoanId and AccrualDate.

The nightly scheduled job will identify missing overdue charges rather than blindly increasing an accumulated fine total. It will reconcile chargeable dates that have begun and have not already been recorded.

Returning a book will also reconcile all applicable overdue charges so that returning a copy before the nightly job executes does not omit the final charge.

The application will use an injectable Java Clock configured with the library's time zone so that date calculations can be tested deterministically.

This design allows the fine accrual process to recover from interrupted processing and prevents duplicate charges for the same loan and overdue date.

The scheduler's persistence and coordination strategy will be documented separately in ADR-003.

### 2.9 Deletion and historical integrity

The application will support create, read, update and delete operations for the required parent-child aggregate.

Physical deletion will be allowed for records without protected historical dependencies.

Appropriate dependent records, such as book-category associations and unused physical copies, may be deleted together with their parent when safe.

Database cascade deletion and application-level CRUD deletion will be treated as separate concerns.

Loans, fine accruals and important reservation history will not be removed through unrestricted cascade deletion.

Deletion attempts involving protected historical records will be rejected with an appropriate response. Titles and copies with such dependencies may instead be withdrawn or deactivated.

These rules preserve borrowing history and fine records without preventing legitimate CRUD operations.

### 2.10 AI purchase suggestions

PurchaseSuggestions stores member requests and the final human review decisions.

AiSuggestionResults stores AI-generated classifications, possible duplicate-title candidates and processing information.

Multiple AI evaluations may be retained for a single purchase suggestion to support retries, repeated classification and review of previous responses.

AI output remains separate from the librarian's final decision.

The AI provider will not directly create, modify or delete catalog records without human review.

The detailed AI provider abstraction, structured output contract, failure handling and testing strategy will be documented in ADR-002.

## 3. Alternatives Considered

### 3.1 MySQL instead of PostgreSQL

MySQL satisfies the assignment's database requirement.

PostgreSQL was selected because its partial unique indexes, transactional behavior and constraint capabilities are particularly useful for enforcing active-loan and reservation invariants.

The consequence is that some schema constraints will use PostgreSQL-specific SQL, reducing portability to another database engine.

### 3.2 One book record instead of separate titles and copies

A simpler model could represent each physical copy as an independent book record.

This was rejected because it would duplicate bibliographic information, complicate inventory management and make reservations against a title with multiple copies more difficult.

Separating titles and copies provides a clearer representation of physical library inventory.

### 3.3 Storing only an accumulated fine total

A single fine balance would require careful tracking of already processed days and would make partial failure recovery more difficult.

Individual daily accrual records were selected because each charge can be uniquely identified, audited and safely reconciled.

The additional storage and database constraints are accepted in exchange for reliability and traceability.

### 3.4 Storing reservations against copies immediately

This approach would require selecting a physical copy when a member joins a waiting queue, even though every copy may currently be borrowed.

Title-level reservations with optional later copy allocation provide a clearer and more flexible model.

### 3.5 Unrestricted cascade deletion

Deleting all dependent records would simplify some operations but could destroy borrowing history, reservation records and fine information.

Selective deletion with historical preservation was chosen instead.

This introduces additional validation but prevents accidental loss of important records.

### 3.6 Eager loading of entity relationships

Eager fetching could simplify access to related data in some operations.

However, it may load unnecessary records and contribute to inefficient queries.

Lazy fetching with explicit query-specific optimizations was selected to provide greater control over database access.

## 4. Consequences

### 4.1 Positive consequences

- The model supports the required library aggregate and ORM relationships.
- Physical inventory is distinguished from bibliographic records.
- Loan, reservation and fine history can be preserved.
- Database constraints provide protection against important integrity violations.
- Daily fine records support idempotent processing and recovery from interrupted execution.
- Transactional services can enforce consistent borrowing and reservation behavior.
- Flyway makes the database schema reproducible across development, testing and deployment.
- Explicit fetching strategies allow performance problems to be investigated and optimized.
- Shared service operations can support both Thymeleaf and REST interfaces.

### 4.2 Negative consequences and trade-offs

- The schema is more complex than a simple CRUD database.
- Loan and reservation operations require careful transaction and concurrency design.
- PostgreSQL-specific constraints reduce portability to other database engines.
- Historical preservation complicates deletion operations.
- Multiple foreign keys to AppUsers increase the complexity of the entity relationships.
- Lazy loading requires deliberate query design for screens and API operations.
- The application must maintain consistency between copy availability, active loans and reservation allocations.
- Individual fine accrual records require more storage and reconciliation logic than a single fine balance.

These costs are accepted because they support the assignment's advanced requirements and improve data integrity.

## 5. Verification Plan

The following checks are planned for later milestones:

- Verify Flyway schema migrations and seed data against PostgreSQL.
- Verify primary keys, foreign keys, unique constraints and partial unique indexes.
- Verify the maximum renewal count and other database check constraints.
- Test concurrent checkout attempts against the same physical copy.
- Test simultaneous checkout requests against the five-active-loan limit.
- Test renewal restrictions and reservation priority.
- Test reservation allocation, expiration and automatic queue advancement.
- Test the handling of damaged or withdrawn allocated copies.
- Execute the fine accrual process multiple times and confirm that no duplicate charges are created.
- Test fine-accrual recovery following interrupted processing.
- Test overdue calculations across month boundaries and daylight-saving changes.
- Test safe deletion, cascade behavior and historical record preservation.
- Demonstrate the required JPA relationship types.
- Identify and resolve an N+1 fetching problem, documenting SQL and measurements before and after optimization.
- Verify the persistence layer through repository tests and Testcontainers integration tests against PostgreSQL.

These checks have not yet been executed.

## 6. Related Documentation

- `docs/diagrams/LibraryLendingSystem_ERD.drawio`
- `docs/decisions.md` (planned)
- ADR-002: AI Integration and Human Review (planned)
- ADR-003: Persistent Scheduling and Job Coordination (planned)

The ER diagram represents the agreed logical design. The final implementation will be checked against it when the database migrations are created.