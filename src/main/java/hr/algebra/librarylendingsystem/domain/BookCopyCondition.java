package hr.algebra.librarylendingsystem.domain;

public enum BookCopyCondition {

    NEW,
    GOOD,
    FAIR,
    POOR,
    DAMAGED;

    public static BookCopyCondition fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Book copy condition is required."
            );
        }

        try {
            return BookCopyCondition.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported book copy condition: " + value,
                    exception
            );
        }
    }
}
