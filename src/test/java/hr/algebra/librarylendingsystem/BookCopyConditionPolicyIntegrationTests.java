package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class BookCopyConditionPolicyIntegrationTests {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreatePoorConditionCopyInMaintenance() {
        BookTitle title = createTitle();

        BookCopyDetails created = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        "POOR",
                        null
                )
        );

        assertEquals("POOR", created.copyCondition());
        assertEquals("MAINTENANCE", created.status());

        entityManager.clear();

        BookCopy persisted = bookCopyRepository
                .findById(created.id())
                .orElseThrow();

        assertEquals("POOR", persisted.getCopyCondition());
        assertEquals("MAINTENANCE", persisted.getStatus());
    }

    @Test
    void shouldMoveAvailableCopyToMaintenanceWhenConditionBecomesPoor() {
        BookTitle title = createTitle();

        BookCopyDetails created = bookCopyService.create(
                title.getId(),
                new BookCopyCommand(
                        uniqueCode(),
                        "GOOD",
                        null
                )
        );

        assertEquals("AVAILABLE", created.status());

        BookCopyDetails updated = bookCopyService.update(
                title.getId(),
                created.id(),
                new BookCopyCommand(
                        created.inventoryCode(),
                        "POOR",
                        null
                )
        );

        assertEquals(created.id(), updated.id());
        assertEquals("POOR", updated.copyCondition());
        assertEquals("MAINTENANCE", updated.status());

        entityManager.clear();

        BookCopy persisted = bookCopyRepository
                .findById(created.id())
                .orElseThrow();

        assertEquals("POOR", persisted.getCopyCondition());
        assertEquals("MAINTENANCE", persisted.getStatus());
    }

    private BookTitle createTitle() {
        return bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Condition Policy Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );
    }

    private String uniqueCode() {
        return "CONDITION-" + UUID.randomUUID();
    }
}
