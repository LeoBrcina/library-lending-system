package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookTitleRepository
        extends JpaRepository<BookTitle, Long>,
        JpaSpecificationExecutor<BookTitle> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT title
            FROM BookTitle title
            WHERE title.id = :titleId
            """)
    Optional<BookTitle> findByIdForUpdate(
            @Param("titleId") Long titleId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM bookcopies
                WHERE booktitleid = :titleId
            )
            """, nativeQuery = true)
    boolean existsPhysicalCopies(
            @Param("titleId") Long titleId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM reservations
                WHERE booktitleid = :titleId
            )
            """, nativeQuery = true)
    boolean existsReservationHistory(
            @Param("titleId") Long titleId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM reservations
                WHERE booktitleid = :titleId
                  AND status IN ('WAITING', 'ALLOCATED')
            )
            """, nativeQuery = true)
    boolean existsActiveReservations(
            @Param("titleId") Long titleId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM loans l
                JOIN bookcopies c
                    ON c.id = l.bookcopyid
                WHERE c.booktitleid = :titleId
                  AND l.returnedat IS NULL
            )
            """, nativeQuery = true)
    boolean existsActiveLoans(
            @Param("titleId") Long titleId
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM bookcopies
                WHERE booktitleid = :titleId
                  AND status IN ('ON_LOAN', 'ON_HOLD')
            )
            """, nativeQuery = true)
    boolean existsCopiesInCirculation(
            @Param("titleId") Long titleId
    );
}