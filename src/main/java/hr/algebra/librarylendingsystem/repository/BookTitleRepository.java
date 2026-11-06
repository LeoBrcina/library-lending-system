package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTitleRepository extends JpaRepository<BookTitle, Long> {
}
