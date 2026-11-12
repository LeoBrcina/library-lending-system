package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookCopyCondition;
import hr.algebra.librarylendingsystem.domain.BookCopyStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookCopyValidationTests {

    @Test
    void shouldAcceptAllSupportedConditions() {
        for (BookCopyCondition condition : BookCopyCondition.values()) {
            BookCopy copy = new BookCopy(
                    "COPY-001",
                    condition.name(),
                    "AVAILABLE"
            );

            assertEquals(condition.name(), copy.getCopyCondition());
        }
    }

    @Test
    void shouldAcceptAllSupportedStatuses() {
        for (BookCopyStatus status : BookCopyStatus.values()) {
            BookCopy copy = new BookCopy(
                    "COPY-001",
                    "GOOD",
                    status.name()
            );

            assertEquals(status.name(), copy.getStatus());
        }
    }

    @Test
    void shouldRejectInvalidCondition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "COPY-001",
                        "UNKNOWN",
                        "AVAILABLE"
                )
        );
    }

    @Test
    void shouldRejectInvalidStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "COPY-001",
                        "GOOD",
                        "BORROWED"
                )
        );
    }

    @Test
    void shouldRejectMissingConditionAndStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "COPY-001",
                        null,
                        "AVAILABLE"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "COPY-001",
                        "GOOD",
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankInventoryCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "   ",
                        "GOOD",
                        "AVAILABLE"
                )
        );
    }

    @Test
    void shouldRejectInventoryCodeLongerThan100Characters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BookCopy(
                        "X".repeat(101),
                        "GOOD",
                        "AVAILABLE"
                )
        );
    }

    @Test
    void shouldNormalizeInventoryCode() {
        BookCopy copy = new BookCopy(
                "  COPY-001  ",
                "GOOD",
                "AVAILABLE"
        );

        assertEquals("COPY-001", copy.getInventoryCode());
    }

    @Test
    void shouldValidateSetterUpdates() {
        BookCopy copy = new BookCopy(
                "COPY-001",
                "GOOD",
                "AVAILABLE"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> copy.setCopyCondition("INVALID")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> copy.setStatus("INVALID")
        );

        assertEquals("GOOD", copy.getCopyCondition());
        assertEquals("AVAILABLE", copy.getStatus());
    }
}
