package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;

public interface BookTitleService {

    BookTitleDetails create(BookTitleCommand command);

    BookTitleDetails findById(Long id);

    BookTitleDetails update(Long id, BookTitleCommand command);

    void deactivate(Long id);
}
