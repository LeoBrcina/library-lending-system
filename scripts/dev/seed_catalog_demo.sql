-- Local development data for the Book Catalog.
-- Run manually against the local PostgreSQL database.
-- Do not add this script to Flyway migrations.
-- Safe to rerun: existing demo titles and copies are skipped.

BEGIN;

CREATE TEMP TABLE demo_catalog_books (
    code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    authors VARCHAR(500) NOT NULL,
    publication_year INTEGER,
    primary_category VARCHAR(50) NOT NULL,
    secondary_category VARCHAR(50),
    copy_count INTEGER NOT NULL,
    active BOOLEAN NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_catalog_books (
    code, title, authors, publication_year,
    primary_category, secondary_category, copy_count, active
)
VALUES
    ('DEMO-001', '[DEMO] The Forgotten Library', 'Alice Morgan', 2021,
     'FICTION', 'MYSTERY_THRILLER', 3, TRUE),

    ('DEMO-002', '[DEMO] A Brief History of Cities', 'Daniel Foster', 2018,
     'HISTORY', NULL, 2, TRUE),

    ('DEMO-003', '[DEMO] Journey Beyond Orion', 'Emma Clarke', 2022,
     'SCI_FI_FANTASY', 'FICTION', 4, TRUE),

    ('DEMO-004', '[DEMO] Introduction to Astronomy', 'James Bennett', 2020,
     'SCIENCE', NULL, 2, TRUE),

    ('DEMO-005', '[DEMO] Java Development Essentials', 'Olivia Reed', 2024,
     'TECHNOLOGY_COMPUTING', NULL, 3, TRUE),

    ('DEMO-006', '[DEMO] The Economics of Tomorrow', 'Lucas Carter', 2019,
     'BUSINESS_ECONOMICS', NULL, 1, TRUE),

    ('DEMO-007', '[DEMO] Lives That Changed History', 'Sophia Turner', 2017,
     'BIOGRAPHY_MEMOIR', 'HISTORY', 2, TRUE),

    ('DEMO-008', '[DEMO] Questions of Philosophy', 'Noah Wilson', 2016,
     'PHILOSOPHY', NULL, 1, TRUE),

    ('DEMO-009', '[DEMO] The Midnight Detective', 'Grace Mitchell', 2023,
     'MYSTERY_THRILLER', 'FICTION', 3, TRUE),

    ('DEMO-010', '[DEMO] Adventures in the Forest', 'Henry Adams', 2021,
     'CHILDRENS_LITERATURE', NULL, 2, TRUE),

    ('DEMO-011', '[DEMO] Understanding Climate', 'Isabella White', 2022,
     'SCIENCE', NULL, 0, TRUE),

    ('DEMO-012', '[DEMO] Foundations of Computing', 'Oliver Brooks', 2015,
     'TECHNOLOGY_COMPUTING', NULL, 2, FALSE);

-- Create missing demonstration titles.
INSERT INTO booktitles (
    isbn, title, authors, publisher,
    publicationyear, description, active
)
SELECT
    d.code,
    d.title,
    d.authors,
    'Demo Publisher',
    d.publication_year,
    'Synthetic development data for catalog testing.',
    d.active
FROM demo_catalog_books d
WHERE NOT EXISTS (
    SELECT 1
    FROM booktitles t
    WHERE t.isbn = d.code
);

-- Assign primary categories.
INSERT INTO bookcategories (
    booktitleid, categoryid, isprimary
)
SELECT
    t.id,
    c.id,
    TRUE
FROM demo_catalog_books d
         JOIN booktitles t ON t.isbn = d.code
         JOIN categories c ON c.code = d.primary_category
    ON CONFLICT (booktitleid, categoryid) DO NOTHING;

-- Assign optional secondary categories.
INSERT INTO bookcategories (
    booktitleid, categoryid, isprimary
)
SELECT
    t.id,
    c.id,
    FALSE
FROM demo_catalog_books d
         JOIN booktitles t ON t.isbn = d.code
         JOIN categories c ON c.code = d.secondary_category
WHERE d.secondary_category IS NOT NULL
    ON CONFLICT (booktitleid, categoryid) DO NOTHING;

-- Create physical copies with unique inventory codes.
INSERT INTO bookcopies (
    booktitleid, inventorycode,
    copycondition, status, acquiredat
)
SELECT
    t.id,
    d.code || '-COPY-' || n.copy_number,
    'GOOD',
    CASE
        WHEN n.copy_number % 3 = 0 THEN 'ON_LOAN'
        ELSE 'AVAILABLE'
        END,
    CURRENT_DATE
FROM demo_catalog_books d
         JOIN booktitles t ON t.isbn = d.code
         CROSS JOIN LATERAL
    generate_series(1, d.copy_count) AS n(copy_number)
ON CONFLICT (inventorycode) DO NOTHING;

COMMIT;

-- Verification.
SELECT
    COUNT(*) AS demo_titles
FROM booktitles
WHERE isbn LIKE 'DEMO-%';

SELECT
    t.title,
    t.active,
    COUNT(bc.id) AS total_copies,
    COUNT(bc.id) FILTER (
        WHERE bc.status = 'AVAILABLE'
    ) AS available_copies
FROM booktitles t
         LEFT JOIN bookcopies bc ON bc.booktitleid = t.id
WHERE t.isbn LIKE 'DEMO-%'
GROUP BY t.id, t.title, t.active
ORDER BY t.title;
