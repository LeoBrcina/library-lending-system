package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookTitleServiceIntegrationTests {

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateBookTitleWithCategories() {
        Long fictionId = categoryId("FICTION");
        Long historyId = categoryId("HISTORY");

        BookTitleCommand command = command(
                List.of(fictionId, historyId),
                fictionId
        );

        BookTitleDetails created = bookTitleService.create(command);

        assertNotNull(created.id());
        assertEquals("Service Test Book", created.title());
        assertEquals("Test Author", created.authors());
        assertEquals(2020, created.publicationYear());
        assertTrue(created.active());

        assertEquals(2, created.categoryIds().size());
        assertTrue(created.categoryIds().contains(fictionId));
        assertTrue(created.categoryIds().contains(historyId));
        assertEquals(fictionId, created.primaryCategoryId());

        entityManager.flush();
        entityManager.clear();

        BookTitleDetails retrieved =
                bookTitleService.findById(created.id());

        assertEquals(created.id(), retrieved.id());
        assertEquals(fictionId, retrieved.primaryCategoryId());

        assertEquals(
                2,
                bookCategoryRepository
                        .findByBookTitle_Id(created.id())
                        .size()
        );
    }

    @Test
    void shouldRejectPrimaryCategoryOutsideSelection() {
        Long fictionId = categoryId("FICTION");
        Long historyId = categoryId("HISTORY");

        BookTitleCommand command = command(
                List.of(fictionId),
                historyId
        );

        InvalidBookTitleException exception = assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.create(command)
        );

        assertTrue(
                exception.getMessage().contains("primary category")
        );
    }

    @Test
    void shouldRejectNonexistentCategory() {
        Long fictionId = categoryId("FICTION");

        BookTitleCommand command = command(
                List.of(fictionId, Long.MAX_VALUE),
                fictionId
        );

        InvalidBookTitleException exception = assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.create(command)
        );

        assertTrue(
                exception.getMessage().contains("do not exist")
        );
    }

    @Test
    void shouldRejectBlankTitle() {
        Long fictionId = categoryId("FICTION");

        BookTitleCommand command = new BookTitleCommand(
                "   ",
                "Test Author",
                null,
                null,
                2020,
                null,
                List.of(fictionId),
                fictionId
        );

        assertThrows(
                InvalidBookTitleException.class,
                () -> bookTitleService.create(command)
        );
    }

    @Test
    void shouldChangePrimaryCategoryAndRemoveOldCategory() {
        Long fictionId = categoryId("FICTION");
        Long historyId = categoryId("HISTORY");
        Long scienceFictionId = categoryId("SCI_FI_FANTASY");

        BookTitleDetails created = bookTitleService.create(
                command(
                        List.of(fictionId, historyId),
                        fictionId
                )
        );

        BookTitleCommand updateCommand = command(
                List.of(historyId, scienceFictionId),
                scienceFictionId
        );

        BookTitleDetails updated = bookTitleService.update(
                created.id(),
                updateCommand
        );

        assertEquals(2, updated.categoryIds().size());
        assertFalse(updated.categoryIds().contains(fictionId));
        assertTrue(updated.categoryIds().contains(historyId));
        assertTrue(updated.categoryIds().contains(scienceFictionId));
        assertEquals(
                scienceFictionId,
                updated.primaryCategoryId()
        );

        entityManager.flush();
        entityManager.clear();

        BookTitleDetails persisted =
                bookTitleService.findById(created.id());

        assertEquals(
                scienceFictionId,
                persisted.primaryCategoryId()
        );

        assertEquals(
                2,
                bookCategoryRepository
                        .findByBookTitle_Id(created.id())
                        .size()
        );

        long primaryCount = bookCategoryRepository
                .findByBookTitle_Id(created.id())
                .stream()
                .filter(association -> association.isPrimary())
                .count();

        assertEquals(1, primaryCount);
    }

    @Test
    void shouldNotDuplicateRepeatedCategorySelections() {
        Long fictionId = categoryId("FICTION");

        BookTitleDetails created = bookTitleService.create(
                command(
                        List.of(fictionId, fictionId),
                        fictionId
                )
        );

        assertEquals(
                List.of(fictionId),
                created.categoryIds()
        );

        assertEquals(
                1,
                bookCategoryRepository
                        .findByBookTitle_Id(created.id())
                        .size()
        );
    }

    @Test
    void shouldPreservePhysicalCopiesWhenUpdatingTitle() {
        Long fictionId = categoryId("FICTION");

        BookTitleDetails created = bookTitleService.create(
                command(
                        List.of(fictionId),
                        fictionId
                )
        );

        BookTitle title = bookTitleRepository
                .findById(created.id())
                .orElseThrow();

        String inventoryCode = "SERVICE-" + UUID.randomUUID();

        BookCopy copy = new BookCopy(
                inventoryCode,
                "GOOD",
                "AVAILABLE"
        );

        title.addCopy(copy);
        bookCopyRepository.saveAndFlush(copy);

        BookTitleCommand updatedCommand = new BookTitleCommand(
                "Updated Service Test Book",
                "Updated Author",
                null,
                "Updated Publisher",
                2021,
                "Updated description",
                List.of(fictionId),
                fictionId
        );

        bookTitleService.update(created.id(), updatedCommand);

        entityManager.flush();
        entityManager.clear();

        BookCopy persistedCopy = bookCopyRepository
                .findByInventoryCode(inventoryCode)
                .orElseThrow();

        assertEquals(created.id(), persistedCopy.getBookTitle().getId());
        assertEquals("GOOD", persistedCopy.getCopyCondition());
        assertEquals("AVAILABLE", persistedCopy.getStatus());

        BookTitleDetails persistedTitle =
                bookTitleService.findById(created.id());

        assertEquals(
                "Updated Service Test Book",
                persistedTitle.title()
        );
    }

    @Test
    void shouldDeactivateBookTitleWithoutDeletingIt() {
        Long fictionId = categoryId("FICTION");

        BookTitleDetails created = bookTitleService.create(
                command(
                        List.of(fictionId),
                        fictionId
                )
        );

        bookTitleService.deactivate(created.id());

        entityManager.flush();
        entityManager.clear();

        BookTitleDetails result =
                bookTitleService.findById(created.id());

        assertFalse(result.active());
        assertTrue(
                bookTitleRepository.existsById(created.id())
        );
    }

    @Test
    void shouldRejectUnknownBookTitle() {
        assertThrows(
                BookTitleNotFoundException.class,
                () -> bookTitleService.findById(Long.MAX_VALUE)
        );
    }

    private Long categoryId(String code) {
        Category category = categoryRepository
                .findByCode(code)
                .orElseThrow();

        return category.getId();
    }

    private BookTitleCommand command(
            List<Long> categoryIds,
            Long primaryCategoryId
    ) {
        return new BookTitleCommand(
                "Service Test Book",
                "Test Author",
                null,
                "Test Publisher",
                2020,
                "Integration test description",
                categoryIds,
                primaryCategoryId
        );
    }
}
