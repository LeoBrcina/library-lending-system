package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookTitleRepository
        extends JpaRepository<BookTitle, Long>,
        JpaSpecificationExecutor<BookTitle> {
}
