package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCopyLookupIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Test
    void shouldFindCopyByTitleAndCopyId() {
        BookTitle title = createTitle();

        BookCopyDetails created = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        "GOOD",
                        LocalDate.of(2026, 11, 10)
                )
        );

        BookCopyDetails found = bookCopyService.findById(
                title.getId(),
                created.id()
        );

        assertEquals(created.id(), found.id());
        assertEquals(title.getId(), found.bookTitleId());
        assertEquals(created.inventoryCode(), found.inventoryCode());
        assertEquals("GOOD", found.copyCondition());
        assertEquals("AVAILABLE", found.status());
        assertEquals(
                LocalDate.of(2026, 11, 10),
                found.acquiredAt()
        );
    }

    @Test
    void shouldRejectCopyBelongingToAnotherTitle() {
        BookTitle firstTitle = createTitle();
        BookTitle secondTitle = createTitle();

        BookCopyDetails created = bookCopyService.create(
                firstTitle.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        "GOOD",
                        null
                )
        );

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.findById(
                        secondTitle.getId(),
                        created.id()
                )
        );
    }

    @Test
    void shouldRejectMissingTitleOrCopy() {
        BookTitle title = createTitle();

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.findById(
                        title.getId(),
                        Long.MAX_VALUE
                )
        );

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookCopyService.findById(
                        Long.MAX_VALUE,
                        1L
                )
        );
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Copy Lookup Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private String uniqueCode() {
        return "LOOKUP-" + UUID.randomUUID();
    }
}
