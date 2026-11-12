package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;

import java.util.List;

public interface BookCopyService {

    BookCopyDetails create(
            Long bookTitleId,
            BookCopyCommand command
    );

    List<BookCopyDetails> findByBookTitleId(Long bookTitleId);

    BookCopyDetails findById(
            Long bookTitleId,
            Long copyId
    );

    BookCopyDetails update(
            Long bookTitleId,
            Long copyId,
            BookCopyCommand command
    );

    BookCopyDetails withdraw(
            Long bookTitleId,
            Long copyId
    );

    void delete(
            Long bookTitleId,
            Long copyId
    );

    BookCopyDetails releaseFromMaintenance(
            Long bookTitleId,
            Long copyId
    );
}
