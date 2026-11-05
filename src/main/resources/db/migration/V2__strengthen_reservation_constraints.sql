
-- V2: Strengthen reservation and loan-return integrity
-- P03 Library Lending System
-- PostgreSQL 17
--
-- Applied after V1__create_library_schema.sql.
--
-- This migration:
-- 1. Defines valid reservation statuses.
-- 2. Enforces consistency between reservation status and fields.
-- 3. Protects active reservations and allocated copies.
-- 4. Ensures an allocated copy belongs to the reserved title.
-- 5. Validates reservation timestamp ordering.
-- 6. Ensures loan returns record the returning librarian.
--
-- FIFO allocation, eligibility checks, pickup expiration,
-- renewals and fines remain transactional service responsibilities.


-- ============================================================
-- 1. RESERVATION STATUS VALUES
-- ============================================================

-- Allowed reservation lifecycle statuses:
--
-- WAITING   : Waiting for an available physical copy.
-- ALLOCATED : Copy assigned; waiting for member pickup.
-- FULFILLED : Member collected the copy; loan issued.
-- CANCELLED : Reservation cancelled.
-- EXPIRED   : Reservation expired without pickup.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_status_values
        CHECK (
            Status IN (
                       'WAITING',
                       'ALLOCATED',
                       'FULFILLED',
                       'CANCELLED',
                       'EXPIRED'
                )
            );


-- ============================================================
-- 2. RESERVATION STATE CONSISTENCY
-- ============================================================

-- A waiting reservation has no physical copy allocation
-- and no allocation or fulfillment timestamps.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_waiting_fields
        CHECK (
            Status <> 'WAITING'
                OR (
                AllocatedCopyId IS NULL
                    AND ReadyAt IS NULL
                    AND PickupDeadline IS NULL
                    AND FulfilledAt IS NULL
                )
            );


-- An allocated reservation must have:
-- - A physical copy
-- - An allocation timestamp
-- - A pickup deadline
-- - No fulfillment timestamp yet

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_allocated_fields
        CHECK (
            Status <> 'ALLOCATED'
                OR (
                AllocatedCopyId IS NOT NULL
                    AND ReadyAt IS NOT NULL
                    AND PickupDeadline IS NOT NULL
                    AND FulfilledAt IS NULL
                )
            );


-- A fulfilled reservation must retain:
-- - The physical copy that was allocated
-- - The allocation timestamp
-- - The original pickup deadline
-- - The fulfillment timestamp
--
-- Keeping these values preserves historical evidence.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_fulfilled_fields
        CHECK (
            Status <> 'FULFILLED'
                OR (
                AllocatedCopyId IS NOT NULL
                    AND ReadyAt IS NOT NULL
                    AND PickupDeadline IS NOT NULL
                    AND FulfilledAt IS NOT NULL
                )
            );


-- A cancelled or expired reservation must not be
-- marked as fulfilled.
--
-- Historical allocation references and timestamps may
-- remain for auditing. They are not active allocations
-- because the reservation status is no longer ALLOCATED.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_inactive_fulfillment
        CHECK (
            Status NOT IN ('CANCELLED', 'EXPIRED')
                OR FulfilledAt IS NULL
            );


-- ============================================================
-- 3. RESERVATION TIMESTAMP CONSISTENCY
-- ============================================================

-- A copy cannot be allocated before the reservation exists.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_ready_after_request
        CHECK (
            ReadyAt IS NULL
                OR ReadyAt >= RequestedAt
            );


-- Fulfillment cannot occur before the original request.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_fulfilled_after_request
        CHECK (
            FulfilledAt IS NULL
                OR FulfilledAt >= RequestedAt
            );


-- When both timestamps exist, fulfillment cannot occur
-- before the copy was allocated.
--
-- Fulfillment is not required to precede the pickup
-- deadline here. Deadline eligibility belongs to the
-- application service and its time-dependent rules.

ALTER TABLE Reservations
    ADD CONSTRAINT ck_reservations_fulfilled_after_ready
        CHECK (
            FulfilledAt IS NULL
                OR ReadyAt IS NULL
                OR FulfilledAt >= ReadyAt
            );


-- V1 already ensures:
-- - PickupDeadline requires ReadyAt.
-- - PickupDeadline must be later than ReadyAt.


-- ============================================================
-- 4. ONE ACTIVE RESERVATION PER MEMBER AND TITLE
-- ============================================================

-- A member cannot create another active reservation
-- for a title while a WAITING or ALLOCATED reservation
-- for that same title already exists.
--
-- Historical FULFILLED, CANCELLED and EXPIRED records
-- do not block future reservations.

CREATE UNIQUE INDEX uq_reservations_active_member_title
    ON Reservations (MemberId, BookTitleId)
    WHERE Status IN ('WAITING', 'ALLOCATED');


-- ============================================================
-- 5. ONE ACTIVE ALLOCATION PER PHYSICAL COPY
-- ============================================================

-- A physical copy cannot be allocated to two members
-- at the same time.
--
-- Only ALLOCATED reservations hold an active allocation.
-- Fulfilled, cancelled and expired reservations may
-- retain historical copy references.

CREATE UNIQUE INDEX uq_reservations_active_copy
    ON Reservations (AllocatedCopyId)
    WHERE Status = 'ALLOCATED';


-- ============================================================
-- 6. ALLOCATED COPY MUST BELONG TO RESERVED TITLE
-- ============================================================

-- The globally unique BookCopies.Id already identifies
-- one copy.
--
-- PostgreSQL additionally requires the referenced
-- column pair to have a unique constraint for a
-- composite foreign key.

ALTER TABLE BookCopies
    ADD CONSTRAINT uq_bookcopies_title_id
        UNIQUE (BookTitleId, Id);


-- Ensure the physical copy belongs to the exact title
-- referenced by the reservation.
--
-- If AllocatedCopyId is NULL, the optional relationship
-- remains valid under PostgreSQL's default MATCH SIMPLE
-- foreign-key behavior.

ALTER TABLE Reservations
    ADD CONSTRAINT fk_reservations_allocated_copy_title
        FOREIGN KEY (BookTitleId, AllocatedCopyId)
            REFERENCES BookCopies (BookTitleId, Id)
            ON DELETE RESTRICT;


-- ============================================================
-- 7. LOAN RETURN CONSISTENCY
-- ============================================================

-- A loan is either:
--
-- Active:
--   ReturnedAt   = NULL
--   ReturnedById = NULL
--
-- Returned:
--   ReturnedAt   = NOT NULL
--   ReturnedById = NOT NULL
--
-- This prevents recording a physical return without
-- identifying the librarian, or assigning a returning
-- librarian to a loan that is still active.

ALTER TABLE Loans
    ADD CONSTRAINT ck_loans_return_fields
        CHECK (
            (
                ReturnedAt IS NULL
                    AND ReturnedById IS NULL
                )
                OR
            (
                ReturnedAt IS NOT NULL
                    AND ReturnedById IS NOT NULL
                )
            );


-- ============================================================
-- END OF V2
-- ============================================================
