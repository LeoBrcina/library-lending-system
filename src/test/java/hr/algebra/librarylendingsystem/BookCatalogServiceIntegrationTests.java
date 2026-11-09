package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogCategory;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogItem;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogSearchCriteria;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookCatalogServiceIntegrationTests {

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldReturnCategoriesAndPhysicalCopyCounts() {
        String marker = uniqueMarker();

        Category fiction = category("FICTION");
        Category history = category("HISTORY");

        BookTitleDetails created = createTitle(
                marker,
                "Catalog Test",
                List.of(fiction.getId(), history.getId()),
                fiction.getId()
        );

        addCopy(created.id(), "AVAILABLE");
        addCopy(created.id(), "AVAILABLE");
        addCopy(created.id(), "ON_LOAN");

        Page<BookCatalogItem> result = search(
                marker,
                null,
                true,
                0,
                10,
                "title",
                true
        );

        assertEquals(1, result.getTotalElements());

        BookCatalogItem item = result.getContent().getFirst();

        assertEquals(created.id(), item.id());
        assertEquals(created.title(), item.title());
        assertEquals("Catalog Test Author", item.authors());
        assertEquals(created.isbn(), item.isbn());
        assertTrue(item.active());

        assertEquals(3L, item.totalCopies());
        assertEquals(2L, item.availableCopies());

        assertEquals(2, item.categories().size());

        BookCatalogCategory primary = item.categories()
                .stream()
                .filter(BookCatalogCategory::primary)
                .findFirst()
                .orElseThrow();

        assertEquals(fiction.getId(), primary.id());
        assertEquals(fiction.getName(), primary.name());

        assertTrue(
                item.categories().stream().anyMatch(
                        category -> category.id().equals(history.getId())
                                && category.name().equals(history.getName())
                                && !category.primary()
                )
        );
    }

    @Test
    void shouldReturnZeroCountsForTitleWithoutCopies() {
        String marker = uniqueMarker();
        Long fictionId = category("FICTION").getId();

        BookTitleDetails created = createTitle(
                marker,
                "No Copies",
                List.of(fictionId),
                fictionId
        );

        Page<BookCatalogItem> result = search(
                marker,
                null,
                true,
                0,
                10,
                "title",
                true
        );

        assertEquals(1, result.getTotalElements());

        BookCatalogItem item = result.getContent().getFirst();

        assertEquals(created.id(), item.id());
        assertEquals(0L, item.totalCopies());
        assertEquals(0L, item.availableCopies());
    }

    @Test
    void shouldApplyCombinedFiltersAndPagination() {
        String marker = uniqueMarker();

        Long fictionId = category("FICTION").getId();
        Long historyId = category("HISTORY").getId();

        BookTitleDetails alpha = createTitle(
                marker,
                "Alpha",
                List.of(fictionId),
                fictionId
        );

        BookTitleDetails beta = createTitle(
                marker,
                "Beta",
                List.of(fictionId),
                fictionId
        );

        createTitle(
                marker,
                "History",
                List.of(historyId),
                historyId
        );

        BookTitleDetails inactive = createTitle(
                marker,
                "Inactive",
                List.of(fictionId),
                fictionId
        );

        bookTitleService.deactivate(inactive.id());

        Page<BookCatalogItem> firstPage = search(
                marker,
                fictionId,
                true,
                0,
                1,
                "title",
                true
        );

        Page<BookCatalogItem> secondPage = search(
                marker,
                fictionId,
                true,
                1,
                1,
                "title",
                true
        );

        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(1, firstPage.getNumberOfElements());

        assertEquals(alpha.id(), firstPage.getContent().getFirst().id());
        assertEquals(beta.id(), secondPage.getContent().getFirst().id());
    }

    @Test
    void shouldSortTitlesInDescendingOrder() {
        String marker = uniqueMarker();
        Long fictionId = category("FICTION").getId();

        BookTitleDetails alpha = createTitle(
                marker,
                "Alpha",
                List.of(fictionId),
                fictionId
        );

        BookTitleDetails zulu = createTitle(
                marker,
                "Zulu",
                List.of(fictionId),
                fictionId
        );

        Page<BookCatalogItem> result = search(
                marker,
                null,
                true,
                0,
                10,
                "title",
                false
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(zulu.id(), result.getContent().get(0).id());
        assertEquals(alpha.id(), result.getContent().get(1).id());
    }

    @Test
    void shouldReturnEmptyPageWhenNothingMatches() {
        Page<BookCatalogItem> result = search(
                "nonexistent-" + UUID.randomUUID(),
                null,
                true,
                0,
                10,
                "title",
                true
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldRejectInvalidSearchParameters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookTitleService.search(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> search(null, null, true, -1, 10, "title", true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> search(null, null, true, 0, 0, "title", true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> search(null, null, true, 0, 101, "title", true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> search(null, -1L, true, 0, 10, "title", true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> search(null, null, true, 0, 10, "invalidField", true)
        );
    }

    private BookTitleDetails createTitle(
            String marker,
            String name,
            List<Long> categoryIds,
            Long primaryCategoryId
    ) {
        return bookTitleService.create(
                new BookTitleCommand(
                        marker + " " + name,
                        "Catalog Test Author",
                        "ISBN-" + UUID.randomUUID().toString().substring(0, 8),
                        "Test Publisher",
                        2020,
                        "Catalog service integration test",
                        categoryIds,
                        primaryCategoryId
                )
        );
    }

    private void addCopy(Long titleId, String status) {
        BookTitle title = bookTitleRepository.findById(titleId)
                .orElseThrow();

        BookCopy copy = new BookCopy(
                "CATALOG-" + UUID.randomUUID(),
                "GOOD",
                status
        );

        title.addCopy(copy);
        bookCopyRepository.saveAndFlush(copy);
    }

    private Category category(String code) {
        return categoryRepository.findByCode(code)
                .orElseThrow();
    }

    private Page<BookCatalogItem> search(
            String text,
            Long categoryId,
            Boolean active,
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        return bookTitleService.search(
                new BookCatalogSearchCriteria(
                        text,
                        categoryId,
                        active,
                        page,
                        size,
                        sortBy,
                        ascending
                )
        );
    }

    private String uniqueMarker() {
        return UUID.randomUUID().toString();
    }
}
