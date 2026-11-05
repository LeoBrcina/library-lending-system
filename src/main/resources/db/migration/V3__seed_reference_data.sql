
-- V3: Seed initial reference data
-- P03 Library Lending System
-- PostgreSQL 17
--
-- Contains stable application reference data only.
-- No demonstration users, credentials, loans or reservations.
--
-- Roles define the application's authorization model.
-- Categories provide an initial public-library taxonomy.

-- ============================================================
-- 1. APPLICATION ROLES
-- ============================================================

INSERT INTO Roles (Name)
VALUES
    ('MEMBER'),
    ('LIBRARIAN'),
    ('ADMIN');


-- ============================================================
-- 2. LIBRARY CATEGORIES
-- ============================================================

INSERT INTO Categories (Code, Name)
VALUES
    ('FICTION', 'Fiction'),
    ('MYSTERY_THRILLER', 'Mystery & Thriller'),
    ('SCI_FI_FANTASY', 'Science Fiction & Fantasy'),
    ('HISTORY', 'History'),
    ('BIOGRAPHY_MEMOIR', 'Biography & Memoir'),
    ('SCIENCE', 'Science'),
    ('TECHNOLOGY_COMPUTING', 'Technology & Computing'),
    ('BUSINESS_ECONOMICS', 'Business & Economics'),
    ('PHILOSOPHY', 'Philosophy'),
    ('CHILDRENS_LITERATURE', 'Children''s Literature');

-- ============================================================
-- END OF V3
-- ============================================================
