package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookTitleServiceRollbackIntegrationTests {

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldRollbackTitleAndCategoriesWhenTransactionFails() {
        Long fictionId = categoryId("FICTION");
        Long historyId = categoryId("HISTORY");

        BookTitleCommand command = new BookTitleCommand(
                "Transaction Rollback Test",
                "Test Author",
                null,
                "Test Publisher",
                2020,
                "This title should not remain in the database.",
                List.of(fictionId, historyId),
                fictionId
        );

        AtomicReference<Long> createdTitleId =
                new AtomicReference<>();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {

                    BookTitleDetails created =
                            bookTitleService.create(command);

                    createdTitleId.set(created.id());

                    assertTrue(
                            bookTitleRepository.existsById(created.id())
                    );

                    assertEquals(
                            2,
                            bookCategoryRepository
                                    .findByBookTitle_Id(created.id())
                                    .size()
                    );

                    // Simulate a failure after the database writes.
                    throw new IllegalStateException(
                            "Simulated failure after saving the title."
                    );
                })
        );

        assertEquals(
                "Simulated failure after saving the title.",
                exception.getMessage()
        );

        Long titleId = createdTitleId.get();

        assertNotNull(titleId);

        // The transaction has ended and must have rolled back.
        assertFalse(
                bookTitleRepository.existsById(titleId)
        );

        assertTrue(
                bookCategoryRepository
                        .findByBookTitle_Id(titleId)
                        .isEmpty()
        );
    }

    private Long categoryId(String code) {
        Category category = categoryRepository
                .findByCode(code)
                .orElseThrow();

        return category.getId();
    }
}
