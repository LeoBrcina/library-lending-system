package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByInventoryCode(String inventoryCode);

    boolean existsByInventoryCode(String inventoryCode);

    List<BookCopy> findByBookTitle_Id(Long bookTitleId);
}
