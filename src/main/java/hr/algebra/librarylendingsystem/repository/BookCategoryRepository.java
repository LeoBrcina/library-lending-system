package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCategoryRepository
        extends JpaRepository<BookCategory, BookCategoryId> {

    List<BookCategory> findByBookTitle_Id(Long bookTitleId);
}
