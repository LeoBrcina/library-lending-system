package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.DuplicateInventoryCodeException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCopyUpdateIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldUpdateAvailableCopyMetadata() {
        BookTitle title = createTitle();
        BookCopyDetails original = createCopy(title, "GOOD");

        String newInventoryCode = uniqueCode();
        LocalDate newAcquisitionDate = LocalDate.of(2026, 11, 12);

        BookCopyDetails updated = bookCopyService.update(
                title.getId(),
                original.id(),
                new BookCopyCommand(
                        "  " + newInventoryCode + "  ",
                        "FAIR",
                        newAcquisitionDate
                )
        );

        assertEquals(original.id(), updated.id());
        assertEquals(title.getId(), updated.bookTitleId());
        assertEquals(newInventoryCode, updated.inventoryCode());
        assertEquals("FAIR", updated.copyCondition());
        assertEquals("AVAILABLE", updated.status());
        assertEquals(newAcquisitionDate, updated.acquiredAt());

        entityManager.clear();

        BookCopy persisted = bookCopyRepository.findById(
                original.id()
        ).orElseThrow();

        assertEquals(newInventoryCode, persisted.getInventoryCode());
        assertEquals("FAIR", persisted.getCopyCondition());
        assertEquals("AVAILABLE", persisted.getStatus());
        assertEquals(newAcquisitionDate, persisted.getAcquiredAt());
    }

    @Test
    void shouldMoveAvailableCopyToMaintenanceWhenDamaged() {
        BookTitle title = createTitle();
        BookCopyDetails original = createCopy(title, "GOOD");

        BookCopyDetails updated = bookCopyService.update(
                title.getId(),
                original.id(),
                new BookCopyCommand(
                        original.inventoryCode(),
                        "DAMAGED",
                        null
                )
        );

        assertEquals("DAMAGED", updated.copyCondition());
        assertEquals("MAINTENANCE", updated.status());

        entityManager.clear();

        BookCopy persisted = bookCopyRepository.findById(
                original.id()
        ).orElseThrow();

        assertEquals("MAINTENANCE", persisted.getStatus());
    }

    @Test
    void shouldKeepRepairedCopyInMaintenance() {
        BookTitle title = createTitle();
        BookCopyDetails original = createCopy(title, "DAMAGED");

        assertEquals("MAINTENANCE", original.status());

        BookCopyDetails updated = bookCopyService.update(
                title.getId(),
                original.id(),
                new BookCopyCommand(
                        original.inventoryCode(),
                        "GOOD",
                        null
                )
        );

        assertEquals("GOOD", updated.copyCondition());
        assertEquals("MAINTENANCE", updated.status());
    }

    @Test
    void shouldRejectDuplicateInventoryCodeOnUpdate() {
        BookTitle title = createTitle();
        BookCopyDetails first = createCopy(title, "GOOD");
        BookCopyDetails second = createCopy(title, "GOOD");

        assertThrows(
                DuplicateInventoryCodeException.class,
                () -> bookCopyService.update(
                        title.getId(),
                        second.id(),
                        new BookCopyCommand(
                                first.inventoryCode(),
                                "FAIR",
                                null
                        )
                )
        );

        entityManager.clear();

        BookCopy persisted = bookCopyRepository.findById(
                second.id()
        ).orElseThrow();

        assertEquals(
                second.inventoryCode(),
                persisted.getInventoryCode()
        );
        assertEquals("GOOD", persisted.getCopyCondition());
    }

    @Test
    void shouldRejectCopyBelongingToDifferentTitle() {
        BookTitle firstTitle = createTitle();
        BookTitle secondTitle = createTitle();
        BookCopyDetails copy = createCopy(firstTitle, "GOOD");

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.update(
                        secondTitle.getId(),
                        copy.id(),
                        new BookCopyCommand(
                                uniqueCode(),
                                "FAIR",
                                null
                        )
                )
        );
    }

    @Test
    void shouldRejectMissingTitleOrCopy() {
        BookTitle title = createTitle();

        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookCopyService.update(
                        Long.MAX_VALUE,
                        1L,
                        new BookCopyCommand(
                                uniqueCode(),
                                "GOOD",
                                null
                        )
                )
        );

        assertThrows(
                BookCopyNotFoundException.class,
                () -> bookCopyService.update(
                        title.getId(),
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
    void shouldRejectEditingProtectedCirculationStatuses() {
        BookTitle title = createTitle();

        for (String status : new String[]{
                "ON_LOAN", "ON_HOLD", "WITHDRAWN"
        }) {
            BookCopyDetails created = createCopy(title, "GOOD");

            BookCopy entity = bookCopyRepository.findById(
                    created.id()
            ).orElseThrow();

            entity.setStatus(status);
            bookCopyRepository.saveAndFlush(entity);

            assertThrows(
                    InvalidBookCopyException.class,
                    () -> bookCopyService.update(
                            title.getId(),
                            created.id(),
                            new BookCopyCommand(
                                    uniqueCode(),
                                    "FAIR",
                                    null
                            )
                    )
            );

            assertEquals("GOOD", entity.getCopyCondition());
            assertEquals(status, entity.getStatus());
        }
    }

    @Test
    void shouldRejectInvalidUpdateWithoutChangingCopy() {
        BookTitle title = createTitle();
        BookCopyDetails original = createCopy(title, "GOOD");

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.update(
                        title.getId(),
                        original.id(),
                        new BookCopyCommand(
                                " ",
                                "FAIR",
                                null
                        )
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.update(
                        title.getId(),
                        original.id(),
                        new BookCopyCommand(
                                uniqueCode(),
                                "UNKNOWN",
                                null
                        )
                )
        );

        assertThrows(
                InvalidBookCopyException.class,
                () -> bookCopyService.update(
                        title.getId(),
                        original.id(),
                        null
                )
        );

        entityManager.clear();

        BookCopy persisted = bookCopyRepository.findById(
                original.id()
        ).orElseThrow();

        assertEquals(
                original.inventoryCode(),
                persisted.getInventoryCode()
        );
        assertEquals("GOOD", persisted.getCopyCondition());
        assertEquals("AVAILABLE", persisted.getStatus());
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Copy Update Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private BookCopyDetails createCopy(
            BookTitle title,
            String condition
    ) {
        return bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        condition,
                        null
                )
        );
    }

    private String uniqueCode() {
        return "UPDATE-" + UUID.randomUUID();
    }
}
