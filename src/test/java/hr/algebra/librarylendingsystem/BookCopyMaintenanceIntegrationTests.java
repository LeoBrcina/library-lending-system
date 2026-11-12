package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCopyMaintenanceIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReleaseRepairedCopyFromMaintenance() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createMaintenanceCopy(title);

        bookCopyService.update(
                title.getId(),
                copy.id(),
                new BookCopyCommand(
                        copy.inventoryCode(),
                        "GOOD",
                        null
                )
        );

        BookCopyDetails released =
                bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        copy.id()
                );

        assertEquals(copy.id(), released.id());
        assertEquals("GOOD", released.copyCondition());
        assertEquals("AVAILABLE", released.status());

        entityManager.clear();

        BookCopy persisted = bookCopyRepository
                .findById(copy.id())
                .orElseThrow();

        assertEquals("AVAILABLE", persisted.getStatus());
        assertEquals("GOOD", persisted.getCopyCondition());
    }

    @Test
    void shouldAcceptNewAndFairConditions() {
        for (String condition : new String[]{"NEW", "FAIR"}) {
            BookTitle title = createTitle();
            BookCopyDetails copy = createMaintenanceCopy(title);

            bookCopyService.update(
                    title.getId(),
                    copy.id(),
                    new BookCopyCommand(
                            copy.inventoryCode(),
                            condition,
                            null
                    )
            );

            BookCopyDetails released =
                    bookCopyService.releaseFromMaintenance(
                            title.getId(),
                            copy.id()
                    );

            assertEquals(condition, released.copyCondition());
            assertEquals("AVAILABLE", released.status());
        }
    }

    @Test
    void shouldRejectDamagedAndPoorConditions() {
        BookTitle title = createTitle();
        BookCopyDetails damagedCopy = createMaintenanceCopy(title);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        damagedCopy.id()
                )
        );

        bookCopyService.update(
                title.getId(),
                damagedCopy.id(),
                new BookCopyCommand(
                        damagedCopy.inventoryCode(),
                        "POOR",
                        null
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        damagedCopy.id()
                )
        );

        assertEquals(
                "MAINTENANCE",
                bookCopyRepository.findById(damagedCopy.id())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldRejectCopiesNotInMaintenance() {
        for (String status : new String[]{
                "AVAILABLE", "ON_LOAN", "ON_HOLD", "WITHDRAWN"
        }) {
            BookTitle title = createTitle();
            BookCopyDetails created = createAvailableCopy(title);

            BookCopy stored = bookCopyRepository
                    .findById(created.id())
                    .orElseThrow();

            stored.setStatus(status);
            bookCopyRepository.saveAndFlush(stored);

            assertThrows(
                    InvalidBookCopyException.class,
                    () -> bookCopyService.releaseFromMaintenance(
                            title.getId(),
                            created.id()
                    )
            );

            assertEquals(status, stored.getStatus());
        }
    }

    @Test
    void shouldRejectInactiveParentTitle() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createMaintenanceCopy(title);

        title.setActive(false);
        bookTitleRepository.saveAndFlush(title);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        copy.id()
                )
        );
    }

    @Test
    void shouldRejectReleaseWhenActiveLoanExists() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createMaintenanceCopy(title);
        repairCopy(title, copy);

        Long userId = createUser();

        jdbcTemplate.update(
                """
                INSERT INTO loans (
                    bookcopyid,
                    memberid,
                    issuedbyid,
                    duedate
                )
                VALUES (?, ?, ?, CURRENT_DATE + 21)
                """,
                copy.id(),
                userId,
                userId
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        copy.id()
                )
        );

        assertTrue(bookCopyRepository.existsActiveLoan(copy.id()));
    }

    @Test
    void shouldRejectReleaseWhenCopyHasActiveAllocation() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createMaintenanceCopy(title);
        repairCopy(title, copy);

        Long userId = createUser();

        jdbcTemplate.update(
                """
                INSERT INTO reservations (
                    booktitleid,
                    memberid,
                    allocatedcopyid,
                    status,
                    readyat,
                    pickupdeadline
                )
                VALUES (
                    ?, ?, ?, 'ALLOCATED',
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP + INTERVAL '3 days'
                )
                """,
                title.getId(),
                userId,
                copy.id()
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        copy.id()
                )
        );

        assertTrue(
                bookCopyRepository.existsActiveAllocation(copy.id())
        );
    }

    @Test
    void shouldRejectReleaseWhenTitleHasWaitingReservations() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createMaintenanceCopy(title);
        repairCopy(title, copy);

        Long userId = createUser();

        jdbcTemplate.update(
                """
                INSERT INTO reservations (
                    booktitleid,
                    memberid,
                    status
                )
                VALUES (?, ?, 'WAITING')
                """,
                title.getId(),
                userId
        );

        assertTrue(
                bookCopyRepository.existsWaitingReservations(title.getId())
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        title.getId(),
                        copy.id()
                )
        );
    }

    @Test
    void shouldRejectCopyBelongingToAnotherTitle() {
        BookTitle firstTitle = createTitle();
        BookTitle secondTitle = createTitle();

        BookCopyDetails copy = createMaintenanceCopy(firstTitle);
        repairCopy(firstTitle, copy);

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.releaseFromMaintenance(
                        secondTitle.getId(),
                        copy.id()
                )
        );

        assertEquals(
                "MAINTENANCE",
                bookCopyRepository.findById(copy.id())
                        .orElseThrow()
                        .getStatus()
        );
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Maintenance Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private BookCopyDetails createMaintenanceCopy(BookTitle title) {
        return bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        "MAINT-" + UUID.randomUUID(),
                        "DAMAGED",
                        null
                )
        );
    }

    private BookCopyDetails createAvailableCopy(BookTitle title) {
        return bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        "AVAILABLE-" + UUID.randomUUID(),
                        "GOOD",
                        null
                )
        );
    }

    private void repairCopy(
            BookTitle title,
            BookCopyDetails copy
    ) {
        bookCopyService.update(
                title.getId(),
                copy.id(),
                new BookCopyCommand(
                        copy.inventoryCode(),
                        "GOOD",
                        null
                )
        );
    }

    private Long createUser() {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO appusers (
                    email,
                    passwordhash,
                    firstname,
                    lastname
                )
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                "maintenance-" + UUID.randomUUID() + "@example.com",
                "test-only-hash",
                "Test",
                "User"
        );
    }
}
