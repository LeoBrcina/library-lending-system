package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.service.dto.BookCatalogItem;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogSearchCriteria;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import org.springframework.data.domain.Page;

public interface BookTitleService {

    BookTitleDetails create(BookTitleCommand command);

    BookTitleDetails findById(Long id);

    BookTitleDetails update(Long id, BookTitleCommand command);

    void deactivate(Long id);

    Page<BookCatalogItem> search(BookCatalogSearchCriteria criteria);
}
