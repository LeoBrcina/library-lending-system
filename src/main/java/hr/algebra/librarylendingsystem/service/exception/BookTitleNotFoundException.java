package hr.algebra.librarylendingsystem.service.exception;

public class BookTitleNotFoundException extends RuntimeException {

    public BookTitleNotFoundException(Long id) {
        super("Book title not found: " + id);
    }
}
