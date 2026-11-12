package hr.algebra.librarylendingsystem.service.exception;

public class BookCopyNotFoundException extends RuntimeException {

    public BookCopyNotFoundException(Long id) {
        super("Book copy not found: " + id);
    }
}
