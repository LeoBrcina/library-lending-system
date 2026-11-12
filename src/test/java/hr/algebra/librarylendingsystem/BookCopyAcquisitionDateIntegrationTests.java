package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCopyAcquisitionDateIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Test
    void shouldRejectFutureAcquisitionDateOnCreation() {
        BookTitle title = createTitle();

        LocalDate futureDate = LocalDate.now(
                ZoneId.of("Europe/Zagreb")
        ).plusDays(2);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.create(
                        title.getId(),
                        new BookCopyCommand(
                                "DATE-" + UUID.randomUUID(),
                                "GOOD",
                                futureDate
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
    void shouldRejectFutureAcquisitionDateOnUpdate() {
        BookTitle title = createTitle();

        BookCopyDetails original = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        "DATE-" + UUID.randomUUID(),
                        "GOOD",
                        null
                )
        );

        LocalDate futureDate = LocalDate.now(
                ZoneId.of("Europe/Zagreb")
        ).plusDays(2);

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.update(
                        title.getId(),
                        original.id(),
                        new BookCopyCommand(
                                original.inventoryCode(),
                                "FAIR",
                                futureDate
                        )
                )
        );

        BookCopyDetails unchanged =
                bookCopyService.findByBookTitleId(
                        title.getId()
                ).getFirst();

        assertEquals("GOOD", unchanged.copyCondition());
        assertNull(unchanged.acquiredAt());
        assertEquals("AVAILABLE", unchanged.status());
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Acquisition Date Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }
}
