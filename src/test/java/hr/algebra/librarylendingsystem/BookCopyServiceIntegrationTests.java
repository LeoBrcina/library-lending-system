package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.DuplicateInventoryCodeException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCopyServiceIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Test
    void shouldCreateAvailableCopyForActiveTitle() {
        BookTitle title = createTitle("Active");

        LocalDate acquiredAt = LocalDate.of(2026, 11, 12);
        String inventoryCode = uniqueCode();

        BookCopyDetails created = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        "  " + inventoryCode + "  ",
                        "GOOD",
                        acquiredAt
                )
        );

        assertNotNull(created.id());
        assertEquals(title.getId(), created.bookTitleId());
        assertEquals(inventoryCode, created.inventoryCode());
        assertEquals("GOOD", created.copyCondition());
        assertEquals("AVAILABLE", created.status());
        assertEquals(acquiredAt, created.acquiredAt());

        List<BookCopyDetails> copies =
                bookCopyService.findByBookTitleId(title.getId());

        assertEquals(1, copies.size());
        assertEquals(created.id(), copies.getFirst().id());
    }

    @Test
    void shouldCreateDamagedCopyInMaintenance() {
        BookTitle title = createTitle("Damaged");

        BookCopyDetails created = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        "DAMAGED",
                        null
                )
        );

        assertEquals("DAMAGED", created.copyCondition());
        assertEquals("MAINTENANCE", created.status());
        assertNull(created.acquiredAt());
    }

    @Test
    void shouldRejectDuplicateInventoryCode() {
        BookTitle firstTitle = createTitle("First");
        BookTitle secondTitle = createTitle("Second");
        String inventoryCode = uniqueCode();

        bookCopyService.create(
                firstTitle.getId(),
                new BookCopyCommand(
                        inventoryCode,
                        "GOOD",
                        null
                )
        );

        assertThrows(
                DuplicateInventoryCodeException.class,
                () -> bookCopyService.create(
                        secondTitle.getId(),
                        new BookCopyCommand(
                                inventoryCode,
                                "FAIR",
                                null
                        )
                )
        );

        assertTrue(
                bookCopyService.findByBookTitleId(
                        secondTitle.getId()
                ).isEmpty()
        );
    }

    @Test
    void shouldRejectCopyCreationForInactiveTitle() {
        BookTitle title = createTitle("Inactive");
        title.setActive(false);
        bookTitleRepository.saveAndFlush(title);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        new BookCopyCommand(
                                uniqueCode(),
                                "GOOD",
                                null
                        )
                )
        );
    }

    @Test
    void shouldRejectCopyCreationForMissingTitle() {
        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookCopyService.create(
                        Long.MAX_VALUE,
                        new BookCopyCommand(
                                uniqueCode(),
                                "GOOD",
                                null
                        )
                )
        );
    }

    @Test
    void shouldRejectInvalidCopyData() {
        BookTitle title = createTitle("Validation");

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        null
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        new BookCopyCommand(
                                " ",
                                "GOOD",
                                null
                        )
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        new BookCopyCommand(
                                "X".repeat(101),
                                "GOOD",
                                null
                        )
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        new BookCopyCommand(
                                uniqueCode(),
                                "UNKNOWN",
                                null
                        )
                )
        );

        assertTrue(
                bookCopyService.findByBookTitleId(
                        title.getId()
                ).isEmpty()
        );
    }

    @Test
    void shouldListOnlyCopiesOfRequestedTitleSortedByInventoryCode() {
        BookTitle firstTitle = createTitle("List A");
        BookTitle secondTitle = createTitle("List B");
        String prefix = "LIST-" + UUID.randomUUID() + "-";

        bookCopyService.create(
                firstTitle.getId(),
                new BookCopyCommand(prefix + "Z", "GOOD", null)
        );

        bookCopyService.create(
                firstTitle.getId(),
                new BookCopyCommand(prefix + "A", "FAIR", null)
        );

        bookCopyService.create(
                secondTitle.getId(),
                new BookCopyCommand(prefix + "M", "GOOD", null)
        );

        List<BookCopyDetails> copies =
                bookCopyService.findByBookTitleId(
                        firstTitle.getId()
                );

        assertEquals(2, copies.size());
        assertEquals(prefix + "A", copies.get(0).inventoryCode());
        assertEquals(prefix + "Z", copies.get(1).inventoryCode());

        assertTrue(
                copies.stream().allMatch(
                        copy -> copy.bookTitleId()
                                .equals(firstTitle.getId())
                )
        );
    }

    private BookTitle createTitle(String name) {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Copy Service Test " + name,
                        "Integration Test Author"
                )
        );
    }

    private String uniqueCode() {
        return "COPY-" + UUID.randomUUID();
    }
}
