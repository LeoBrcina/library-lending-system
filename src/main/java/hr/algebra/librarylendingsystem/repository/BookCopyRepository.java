package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByInventoryCode(String inventoryCode);

    boolean existsByInventoryCode(String inventoryCode);

    List<BookCopy> findByBookTitle_Id(Long bookTitleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT copy
            FROM BookCopy copy
            WHERE copy.id = :copyId
            """)
    Optional<BookCopy> findByIdForUpdate(
            @Param("copyId") Long copyId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM loans
                WHERE bookcopyid = :copyId
            )
            """, nativeQuery = true)
    boolean existsLoanHistory(
            @Param("copyId") Long copyId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM loans
                WHERE bookcopyid = :copyId
                  AND returnedat IS NULL
            )
            """, nativeQuery = true)
    boolean existsActiveLoan(
            @Param("copyId") Long copyId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM reservations
                WHERE allocatedcopyid = :copyId
            )
            """, nativeQuery = true)
    boolean existsReservationHistory(
            @Param("copyId") Long copyId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM reservations
                WHERE allocatedcopyid = :copyId
                  AND status = 'ALLOCATED'
            )
            """, nativeQuery = true)
    boolean existsActiveAllocation(
            @Param("copyId") Long copyId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM reservations
                WHERE booktitleid = :bookTitleId
                  AND status = 'WAITING'
            )
            """, nativeQuery = true)
    boolean existsWaitingReservations(
            @Param("bookTitleId") Long bookTitleId
    );

    @Query("""
            SELECT
                copy.bookTitle.id AS bookTitleId,
                COUNT(copy) AS totalCopies,
                SUM(
                    CASE
                        WHEN copy.status = 'AVAILABLE' THEN 1L
                        ELSE 0L
                    END
                ) AS availableCopies
            FROM BookCopy copy
            WHERE copy.bookTitle.id IN :bookTitleIds
            GROUP BY copy.bookTitle.id
            """)
    List<BookCopyCountView> countCopiesByBookTitleIds(
            @Param("bookTitleIds") Collection<Long> bookTitleIds
    );

    interface BookCopyCountView {

        Long getBookTitleId();

        Long getTotalCopies();

        Long getAvailableCopies();
    }
}
