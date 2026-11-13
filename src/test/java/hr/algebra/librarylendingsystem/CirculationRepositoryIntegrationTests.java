package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.AppUser;
import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.FineAccrual;
import hr.algebra.librarylendingsystem.domain.Loan;
import hr.algebra.librarylendingsystem.domain.Reservation;
import hr.algebra.librarylendingsystem.domain.ReservationStatus;
import hr.algebra.librarylendingsystem.repository.AppUserRepository;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.FineAccrualRepository;
import hr.algebra.librarylendingsystem.repository.LoanRepository;
import hr.algebra.librarylendingsystem.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CirculationRepositoryIntegrationTests {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineAccrualRepository fineAccrualRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveWaitingReservation() {
        BookTitle title = createTitle();
        AppUser member = createUser();

        Reservation reservation = new Reservation(title, member);

        reservation = reservationRepository.saveAndFlush(reservation);

        Long reservationId = reservation.getId();

        entityManager.clear();

        Reservation savedReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertEquals(
                ReservationStatus.WAITING,
                savedReservation.getStatus()
        );

        assertEquals(
                title.getId(),
                savedReservation.getBookTitle().getId()
        );

        assertEquals(
                member.getId(),
                savedReservation.getMember().getId()
        );

        assertNull(savedReservation.getAllocatedCopy());
        assertNull(savedReservation.getReadyAt());
        assertNull(savedReservation.getPickupDeadline());
        assertNull(savedReservation.getFulfilledAt());

        assertNotNull(savedReservation.getRequestedAt());
    }

    @Test
    void shouldFindWaitingReservationsInFifoOrder() {
        BookTitle title = createTitle();

        Reservation first = reservationRepository.saveAndFlush(
                new Reservation(title, createUser())
        );

        Reservation second = reservationRepository.saveAndFlush(
                new Reservation(title, createUser())
        );

        entityManager.clear();

        List<Reservation> waitingReservations =
                reservationRepository
                        .findByBookTitle_IdAndStatusOrderByRequestedAtAscIdAsc(
                                title.getId(),
                                ReservationStatus.WAITING
                        );

        assertEquals(2, waitingReservations.size());

        assertEquals(
                first.getId(),
                waitingReservations.get(0).getId()
        );

        assertEquals(
                second.getId(),
                waitingReservations.get(1).getId()
        );
    }

    @Test
    void shouldRejectDuplicateActiveReservation() {
        BookTitle title = createTitle();
        AppUser member = createUser();

        Reservation first = new Reservation(title, member);

        reservationRepository.saveAndFlush(first);

        Reservation duplicate = new Reservation(title, member);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> reservationRepository.saveAndFlush(duplicate)
        );
    }

    @Test
    void shouldSaveAndRetrieveLoan() {
        BookCopy copy = createCopy();
        AppUser member = createUser();
        AppUser librarian = createUser();

        LocalDate dueDate = LocalDate.now().plusDays(21);

        Loan loan = new Loan(
                copy,
                member,
                librarian,
                dueDate
        );

        loan = loanRepository.saveAndFlush(loan);

        Long loanId = loan.getId();

        entityManager.clear();

        Loan savedLoan = loanRepository
                .findById(loanId)
                .orElseThrow();

        assertEquals(
                copy.getId(),
                savedLoan.getBookCopy().getId()
        );

        assertEquals(
                member.getId(),
                savedLoan.getMember().getId()
        );

        assertEquals(
                librarian.getId(),
                savedLoan.getIssuedBy().getId()
        );

        assertEquals(dueDate, savedLoan.getDueDate());

        assertNotNull(savedLoan.getCheckedOutAt());
        assertNotNull(savedLoan.getVersion());

        assertEquals(0, savedLoan.getRenewalCount());

        assertNull(savedLoan.getReturnedAt());
        assertNull(savedLoan.getReturnedBy());
        assertNull(savedLoan.getReservation());

        assertTrue(
                loanRepository.existsByBookCopy_IdAndReturnedAtIsNull(
                        copy.getId()
                )
        );
    }

    @Test
    void shouldRejectTwoActiveLoansForSameCopy() {
        BookCopy copy = createCopy();
        AppUser librarian = createUser();

        Loan firstLoan = new Loan(
                copy,
                createUser(),
                librarian,
                LocalDate.now().plusDays(21)
        );

        loanRepository.saveAndFlush(firstLoan);

        Loan secondLoan = new Loan(
                copy,
                createUser(),
                librarian,
                LocalDate.now().plusDays(21)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> loanRepository.saveAndFlush(secondLoan)
        );
    }

    @Test
    void shouldIncrementVersionWhenLoanIsUpdated() {
        Loan loan = createLoan();

        Long loanId = loan.getId();
        Long originalVersion = loan.getVersion();

        entityManager.clear();

        Loan savedLoan = loanRepository
                .findById(loanId)
                .orElseThrow();

        savedLoan.setDueDate(
                savedLoan.getDueDate().plusDays(14)
        );

        loanRepository.saveAndFlush(savedLoan);

        assertEquals(
                originalVersion + 1,
                savedLoan.getVersion()
        );
    }

    @Test
    void shouldRejectStaleLoanUpdate() {
        Loan loan = createLoan();

        Long loanId = loan.getId();

        entityManager.clear();

        Loan staleLoan = loanRepository
                .findById(loanId)
                .orElseThrow();

        entityManager.detach(staleLoan);

        Loan currentLoan = loanRepository
                .findById(loanId)
                .orElseThrow();

        currentLoan.setDueDate(
                currentLoan.getDueDate().plusDays(14)
        );

        entityManager.flush();

        staleLoan.setDueDate(
                staleLoan.getDueDate().plusDays(7)
        );

        assertThrows(
                OptimisticLockException.class,
                () -> {
                    entityManager.merge(staleLoan);
                    entityManager.flush();
                }
        );
    }

    @Test
    void shouldSaveAndRetrieveFineAccrual() {
        Loan loan = createLoan();

        LocalDate accrualDate = LocalDate.now();

        FineAccrual fine = new FineAccrual(
                loan,
                accrualDate,
                new BigDecimal("0.20")
        );

        fine = fineAccrualRepository.saveAndFlush(fine);

        Long fineId = fine.getId();

        entityManager.clear();

        FineAccrual savedFine = fineAccrualRepository
                .findById(fineId)
                .orElseThrow();

        assertEquals(
                loan.getId(),
                savedFine.getLoan().getId()
        );

        assertEquals(
                accrualDate,
                savedFine.getAccrualDate()
        );

        assertEquals(
                0,
                new BigDecimal("0.20").compareTo(
                        savedFine.getAmount()
                )
        );

        assertTrue(
                fineAccrualRepository.existsByLoan_IdAndAccrualDate(
                        loan.getId(),
                        accrualDate
                )
        );
    }

    @Test
    void shouldRejectDuplicateDailyFineAccrual() {
        Loan loan = createLoan();

        LocalDate accrualDate = LocalDate.now();

        FineAccrual firstFine = new FineAccrual(
                loan,
                accrualDate,
                new BigDecimal("0.20")
        );

        fineAccrualRepository.saveAndFlush(firstFine);

        FineAccrual duplicateFine = new FineAccrual(
                loan,
                accrualDate,
                new BigDecimal("0.20")
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> fineAccrualRepository.saveAndFlush(
                        duplicateFine
                )
        );
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Circulation Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
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

    private BookCopy createCopy() {
        BookTitle title = createTitle();

        BookCopy copy = new BookCopy(
                "CIRC-" + UUID.randomUUID(),
                "GOOD",
                "AVAILABLE"
        );

        title.addCopy(copy);

        return bookCopyRepository.saveAndFlush(copy);
    }

    private Loan createLoan() {
        Loan loan = new Loan(
                createCopy(),
                createUser(),
                createUser(),
                LocalDate.now().plusDays(21)
        );

        return loanRepository.saveAndFlush(loan);
    }
}