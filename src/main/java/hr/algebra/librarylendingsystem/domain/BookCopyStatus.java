package hr.algebra.librarylendingsystem.domain;

public enum BookCopyStatus {

    AVAILABLE,
    ON_LOAN,
    ON_HOLD,
    MAINTENANCE,
    WITHDRAWN;

    public static BookCopyStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Book copy status is required."
            );
        }

        try {
            return BookCopyStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported book copy status: " + value,
                    exception
            );
        }
    }
}
