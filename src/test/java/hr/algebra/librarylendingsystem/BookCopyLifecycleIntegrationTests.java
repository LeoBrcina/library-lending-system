package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
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
class BookCopyLifecycleIntegrationTests {

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
    void shouldWithdrawAvailableCopy() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");

        BookCopyDetails withdrawn = bookCopyService.withdraw(
                title.getId(),
                copy.id()
        );

        assertEquals(copy.id(), withdrawn.id());
        assertEquals("WITHDRAWN", withdrawn.status());
        assertEquals("GOOD", withdrawn.copyCondition());

        entityManager.clear();

        assertEquals(
                "WITHDRAWN",
                bookCopyRepository.findById(copy.id())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldWithdrawCopyInMaintenance() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "DAMAGED");

        assertEquals("MAINTENANCE", copy.status());

        BookCopyDetails withdrawn = bookCopyService.withdraw(
                title.getId(),
                copy.id()
        );

        assertEquals("WITHDRAWN", withdrawn.status());
        assertEquals("DAMAGED", withdrawn.copyCondition());
    }

    @Test
    void shouldAllowWithdrawalAfterLoanHasBeenReturned() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");

        Long userId = createUser();
        insertLoan(copy.id(), userId, true);

        BookCopyDetails withdrawn = bookCopyService.withdraw(
                title.getId(),
                copy.id()
        );

        assertEquals("WITHDRAWN", withdrawn.status());
        assertTrue(bookCopyRepository.existsLoanHistory(copy.id()));
        assertFalse(bookCopyRepository.existsActiveLoan(copy.id()));
    }

    @Test
    void shouldRejectWithdrawalWhenActiveLoanExists() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");

        Long userId = createUser();
        insertLoan(copy.id(), userId, false);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.withdraw(
                        title.getId(),
                        copy.id()
                )
        );

        assertEquals(
                "AVAILABLE",
                bookCopyRepository.findById(copy.id())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldRejectWithdrawalWhenReservationIsAllocated() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");
        Long userId = createUser();

        insertReservation(
                title.getId(),
                copy.id(),
                userId,
                "ALLOCATED"
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.withdraw(
                        title.getId(),
                        copy.id()
                )
        );

        assertTrue(
                bookCopyRepository.existsActiveAllocation(copy.id())
        );
    }

    @Test
    void shouldRejectWithdrawalForProtectedStatus() {
        for (String status : new String[]{"ON_LOAN", "ON_HOLD"}) {
            BookTitle title = createTitle();
            BookCopyDetails copy = createCopy(title, "GOOD");

            BookCopy stored = bookCopyRepository.findById(
                    copy.id()
            ).orElseThrow();

            stored.setStatus(status);
            bookCopyRepository.saveAndFlush(stored);

            assertThrows(
                    InvalidBookCopyException.class,
                    () -> bookCopyService.withdraw(
                            title.getId(),
                            copy.id()
                    )
            );
        }
    }

    @Test
    void shouldDeleteUnusedPhysicalCopy() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");

        bookCopyService.delete(title.getId(), copy.id());

        entityManager.clear();

        assertFalse(bookCopyRepository.existsById(copy.id()));
    }

    @Test
    void shouldRejectDeletionWhenReturnedLoanHistoryExists() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");

        Long userId = createUser();
        insertLoan(copy.id(), userId, true);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.delete(
                        title.getId(),
                        copy.id()
                )
        );

        assertTrue(bookCopyRepository.existsById(copy.id()));
    }

    @Test
    void shouldRejectDeletionWhenReservationHistoryExists() {
        BookTitle title = createTitle();
        BookCopyDetails copy = createCopy(title, "GOOD");
        Long userId = createUser();

        insertReservation(
                title.getId(),
                copy.id(),
                userId,
                "CANCELLED"
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.delete(
                        title.getId(),
                        copy.id()
                )
        );

        assertTrue(bookCopyRepository.existsById(copy.id()));
    }

    @Test
    void shouldRejectMissingOrIncorrectParentAndCopy() {
        BookTitle firstTitle = createTitle();
        BookTitle secondTitle = createTitle();
        BookCopyDetails copy = createCopy(firstTitle, "GOOD");

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.delete(
                        secondTitle.getId(),
                        copy.id()
                )
        );

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.withdraw(
                        firstTitle.getId(),
                        Long.MAX_VALUE
                )
        );

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookCopyService.delete(
                        Long.MAX_VALUE,
                        copy.id()
                )
        );

        assertTrue(bookCopyRepository.existsById(copy.id()));
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Lifecycle Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private BookCopyDetails createCopy(
            BookTitle title,
            String condition
    ) {
        return bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        "LIFECYCLE-" + UUID.randomUUID(),
                        condition,
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
                "test-" + UUID.randomUUID() + "@example.com",
                "test-only-hash",
                "Test",
                "User"
        );
    }

    private void insertLoan(
            Long copyId,
            Long userId,
            boolean returned
    ) {
        if (returned) {
            jdbcTemplate.update(
                    """
                    INSERT INTO loans (
                        bookcopyid,
                        memberid,
                        issuedbyid,
                        returnedbyid,
                        duedate,
                        returnedat
                    )
                    VALUES (?, ?, ?, ?, CURRENT_DATE, CURRENT_TIMESTAMP)
                    """,
                    copyId,
                    userId,
                    userId,
                    userId
            );
        } else {
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
                    copyId,
                    userId,
                    userId
            );
        }
    }

    private void insertReservation(
            Long titleId,
            Long copyId,
            Long userId,
            String status
    ) {
        if ("ALLOCATED".equals(status)) {
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
                    titleId,
                    userId,
                    copyId
            );
        } else {
            jdbcTemplate.update(
                    """
                    INSERT INTO reservations (
                        booktitleid,
                        memberid,
                        allocatedcopyid,
                        status
                    )
                    VALUES (?, ?, ?, ?)
                    """,
                    titleId,
                    userId,
                    copyId,
                    status
            );
        }
    }
}
