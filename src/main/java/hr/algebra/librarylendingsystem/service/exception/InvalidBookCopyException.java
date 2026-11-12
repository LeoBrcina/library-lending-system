package hr.algebra.librarylendingsystem.service.exception;

public class InvalidBookCopyException extends RuntimeException {

    public InvalidBookCopyException(String message) {
        super(message);
    }
}
