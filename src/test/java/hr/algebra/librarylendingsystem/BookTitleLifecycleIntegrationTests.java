package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.AppUser;
import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.domain.Loan;
import hr.algebra.librarylendingsystem.domain.Reservation;
import hr.algebra.librarylendingsystem.domain.ReservationStatus;
import hr.algebra.librarylendingsystem.repository.AppUserRepository;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.repository.LoanRepository;
import hr.algebra.librarylendingsystem.repository.ReservationRepository;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookTitleLifecycleIntegrationTests {

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldDeactivateBookTitleWithoutDeletingIt() {
        BookTitle title = createTitle();

        bookTitleService.deactivate(title.getId());

        entityManager.flush();
        entityManager.clear();

        BookTitle savedTitle = bookTitleRepository
                .findById(title.getId())
                .orElseThrow();

        assertFalse(savedTitle.isActive());

        assertTrue(
                bookTitleRepository.existsById(title.getId())
        );
    }

    @Test
    void shouldPreservePhysicalCopiesWhenDeactivatingTitle() {
        BookTitle title = createTitle();
        BookCopy copy = createCopy(title, "AVAILABLE");

        bookTitleService.deactivate(title.getId());

        entityManager.flush();
        entityManager.clear();

        BookTitle savedTitle = bookTitleRepository
                .findById(title.getId())
                .orElseThrow();

        BookCopy savedCopy = bookCopyRepository
                .findById(copy.getId())
                .orElseThrow();

        assertFalse(savedTitle.isActive());

        assertEquals(
                title.getId(),
                savedCopy.getBookTitle().getId()
        );

        assertEquals("AVAILABLE", savedCopy.getStatus());
    }

    @Test
    void shouldRejectDeactivationWhenActiveLoanExists() {
        BookTitle title = createTitle();
        BookCopy copy = createCopy(title, "ON_LOAN");

        AppUser member = createUser();
        AppUser librarian = createUser();

        Loan loan = new Loan(
                copy,
                member,
                librarian,
                LocalDate.now().plusDays(21)
        );

        loanRepository.saveAndFlush(loan);

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.deactivate(title.getId())
        );

        entityManager.clear();

        assertTrue(
                bookTitleRepository.findById(title.getId())
                        .orElseThrow()
                        .isActive()
        );

        assertTrue(
                loanRepository.existsByBookCopy_IdAndReturnedAtIsNull(
                        copy.getId()
                )
        );
    }

    @Test
    void shouldRejectDeactivationWhenWaitingReservationExists() {
        BookTitle title = createTitle();
        AppUser member = createUser();

        Reservation reservation = new Reservation(title, member);

        reservationRepository.saveAndFlush(reservation);

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.deactivate(title.getId())
        );

        entityManager.clear();

        assertTrue(
                bookTitleRepository.findById(title.getId())
                        .orElseThrow()
                        .isActive()
        );
    }

    @Test
    void shouldRejectDeactivationWhenCopyHasProtectedStatus() {
        BookTitle title = createTitle();
        createCopy(title, "ON_HOLD");

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.deactivate(title.getId())
        );

        entityManager.clear();

        assertTrue(
                bookTitleRepository.findById(title.getId())
                        .orElseThrow()
                        .isActive()
        );
    }

    @Test
    void shouldRejectDeactivationOfAlreadyInactiveTitle() {
        BookTitle title = createTitle();

        bookTitleService.deactivate(title.getId());

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.deactivate(title.getId())
        );

        entityManager.clear();

        assertFalse(
                bookTitleRepository.findById(title.getId())
                        .orElseThrow()
                        .isActive()
        );
    }

    @Test
    void shouldDeleteUnusedBookTitle() {
        BookTitle title = createTitle();

        Long titleId = title.getId();

        bookTitleService.delete(titleId);

        entityManager.clear();

        assertFalse(
                bookTitleRepository.existsById(titleId)
        );
    }

    @Test
    void shouldDeleteBookTitleAndItsCategoryAssociations() {
        Category category = categoryRepository
                .findByCode("FICTION")
                .orElseThrow();

        Long categoryId = category.getId();

        BookTitleCommand command = new BookTitleCommand(
                "Category Deletion Test " + UUID.randomUUID(),
                "Integration Test Author",
                null,
                null,
                2020,
                null,
                List.of(categoryId),
                categoryId
        );

        BookTitleDetails created = bookTitleService.create(command);

        Long titleId = created.id();

        assertEquals(
                1,
                bookCategoryRepository.findByBookTitle_Id(titleId).size()
        );

        entityManager.flush();
        entityManager.clear();

        bookTitleService.delete(titleId);

        entityManager.flush();
        entityManager.clear();

        assertFalse(
                bookTitleRepository.existsById(titleId)
        );

        assertTrue(
                bookCategoryRepository.findByBookTitle_Id(titleId).isEmpty()
        );

        assertTrue(
                categoryRepository.existsById(categoryId)
        );
    }

    @Test
    void shouldRejectDeletionWhenPhysicalCopiesExist() {
        BookTitle title = createTitle();
        BookCopy copy = createCopy(title, "AVAILABLE");

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.delete(title.getId())
        );

        entityManager.clear();

        assertTrue(
                bookTitleRepository.existsById(title.getId())
        );

        assertTrue(
                bookCopyRepository.existsById(copy.getId())
        );
    }

    @Test
    void shouldRejectDeletionWhenReservationHistoryExists() {
        BookTitle title = createTitle();
        AppUser member = createUser();

        Reservation reservation = new Reservation(title, member);
        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.saveAndFlush(reservation);

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.delete(title.getId())
        );

        entityManager.clear();

        assertTrue(
                bookTitleRepository.existsById(title.getId())
        );

        assertTrue(
                reservationRepository.existsById(reservation.getId())
        );
    }

    @Test
    void shouldRejectMissingBookTitleForLifecycleOperations() {
        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookTitleService.deactivate(Long.MAX_VALUE)
        );

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookTitleService.delete(Long.MAX_VALUE)
        );

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookTitleService.deactivate(null)
        );

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookTitleService.delete(null)
        );
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Lifecycle Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private BookCopy createCopy(
            BookTitle title,
            String status
    ) {
        BookCopy copy = new BookCopy(
                "TITLE-LIFECYCLE-" + UUID.randomUUID(),
                "GOOD",
                status
        );

        title.addCopy(copy);

        return bookCopyRepository.saveAndFlush(copy);
    }

    private AppUser createUser() {
        return appUserRepository.saveAndFlush(
                new AppUser(
                        "test-" + UUID.randomUUID() + "@example.com",
                        "test-password-hash",
                        "Test",
                        "User"
                )
        );
    }
}