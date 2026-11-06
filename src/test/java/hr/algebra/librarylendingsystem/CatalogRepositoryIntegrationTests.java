package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookCategoryId;
import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogRepositoryIntegrationTests {

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveBookTitleWithPhysicalCopies() {
        BookTitle title = new BookTitle(
                "Integration Test Book",
                "Test Author"
        );

        title.setPublicationYear(2020);
        title = bookTitleRepository.saveAndFlush(title);

        String inventoryCode = "TEST-" + UUID.randomUUID();

        BookCopy copy = new BookCopy(
                inventoryCode,
                "GOOD",
                "AVAILABLE"
        );

        title.addCopy(copy);
        bookCopyRepository.saveAndFlush(copy);

        Long titleId = title.getId();

        entityManager.clear();

        BookTitle savedTitle = bookTitleRepository
                .findById(titleId)
                .orElseThrow();

        BookCopy savedCopy = bookCopyRepository
                .findByInventoryCode(inventoryCode)
                .orElseThrow();

        assertEquals("Integration Test Book", savedTitle.getTitle());
        assertEquals("Test Author", savedTitle.getAuthors());
        assertEquals(2020, savedTitle.getPublicationYear());

        assertEquals(titleId, savedCopy.getBookTitle().getId());
        assertEquals("GOOD", savedCopy.getCopyCondition());
        assertEquals("AVAILABLE", savedCopy.getStatus());

        assertTrue(bookCopyRepository.existsByInventoryCode(inventoryCode));

        List<BookCopy> copies = bookCopyRepository
                .findByBookTitle_Id(titleId);

        assertEquals(1, copies.size());
        assertEquals(inventoryCode, copies.getFirst().getInventoryCode());
    }

    @Test
    void shouldSaveAndRetrieveBookCategoryAssociations() {
        Category fiction = categoryRepository
                .findByCode("FICTION")
                .orElseThrow();

        Category history = categoryRepository
                .findByCode("HISTORY")
                .orElseThrow();

        BookTitle title = new BookTitle(
                "Category Integration Test",
                "Test Author"
        );

        title = bookTitleRepository.saveAndFlush(title);

        BookCategory primaryCategory = new BookCategory(
                title,
                fiction,
                true
        );

        BookCategory secondaryCategory = new BookCategory(
                title,
                history,
                false
        );

        bookCategoryRepository.save(primaryCategory);
        bookCategoryRepository.save(secondaryCategory);
        bookCategoryRepository.flush();

        Long titleId = title.getId();

        entityManager.clear();

        BookCategoryId fictionId = new BookCategoryId(
                titleId,
                fiction.getId()
        );

        BookCategoryId historyId = new BookCategoryId(
                titleId,
                history.getId()
        );

        BookCategory savedPrimary = bookCategoryRepository
                .findById(fictionId)
                .orElseThrow();

        BookCategory savedSecondary = bookCategoryRepository
                .findById(historyId)
                .orElseThrow();

        assertTrue(savedPrimary.isPrimary());
        assertFalse(savedSecondary.isPrimary());

        assertEquals("FICTION", savedPrimary.getCategory().getCode());
        assertEquals("HISTORY", savedSecondary.getCategory().getCode());

        List<BookCategory> associations =
                bookCategoryRepository.findByBookTitle_Id(titleId);

        assertEquals(2, associations.size());

        long primaryCount = associations.stream()
                .filter(BookCategory::isPrimary)
                .count();

        assertEquals(1, primaryCount);
    }

    @Test
    void shouldRejectTwoPrimaryCategoriesForSameBookTitle() {
        Category fiction = categoryRepository
                .findByCode("FICTION")
                .orElseThrow();

        Category history = categoryRepository
                .findByCode("HISTORY")
                .orElseThrow();

        BookTitle title = bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Primary Category Constraint Test",
                        "Test Author"
                )
        );

        BookCategory firstPrimary = new BookCategory(
                title,
                fiction,
                true
        );

        bookCategoryRepository.saveAndFlush(firstPrimary);

        BookCategory secondPrimary = new BookCategory(
                title,
                history,
                true
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> bookCategoryRepository.saveAndFlush(secondPrimary)
        );
    }
}
