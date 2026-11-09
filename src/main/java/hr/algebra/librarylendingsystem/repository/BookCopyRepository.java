package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByInventoryCode(String inventoryCode);

    boolean existsByInventoryCode(String inventoryCode);

    List<BookCopy> findByBookTitle_Id(Long bookTitleId);

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
