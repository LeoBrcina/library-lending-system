package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleSpecifications;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogSearchRepositoryIntegrationTests {

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSearchByTitleAuthorAndIsbn() {
        String marker = uniqueMarker();

        BookTitle title = new BookTitle(
                "Catalog " + marker + " Mystery",
                "Writer " + marker
        );

        title.setIsbn("ISBN-" + marker);
        title = bookTitleRepository.saveAndFlush(title);

        Long expectedId = title.getId();

        assertSearchFindsId(marker.toUpperCase(), expectedId);
        assertSearchFindsId("Writer " + marker, expectedId);
        assertSearchFindsId("ISBN-" + marker, expectedId);
    }

    @Test
    void shouldCombineCategoryAndActiveStatusFilters() {
        String marker = uniqueMarker();

        Category fiction = category("FICTION");
        Category history = category("HISTORY");

        BookTitle activeFiction = saveTitle(
                "Catalog " + marker + " Active Fiction",
                true
        );

        BookTitle inactiveFiction = saveTitle(
                "Catalog " + marker + " Inactive Fiction",
                false
        );

        BookTitle activeHistory = saveTitle(
                "Catalog " + marker + " Active History",
                true
        );

        addCategory(activeFiction, fiction, true);
        addCategory(inactiveFiction, fiction, true);
        addCategory(activeHistory, history, true);

        Specification<BookTitle> specification = Specification.allOf(
                BookTitleSpecifications.containsText(marker),
                BookTitleSpecifications.hasCategory(fiction.getId()),
                BookTitleSpecifications.hasActiveStatus(true)
        );

        Page<BookTitle> results = bookTitleRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, results.getTotalElements());
        assertEquals(activeFiction.getId(), results.getContent().getFirst().getId());
    }

    @Test
    void shouldPaginateResultsWithoutDuplicates() {
        String marker = uniqueMarker();

        Category fiction = category("FICTION");
        Category history = category("HISTORY");

        BookTitle alpha = saveTitle(
                "Catalog " + marker + " Alpha",
                true
        );

        BookTitle beta = saveTitle(
                "Catalog " + marker + " Beta",
                true
        );

        BookTitle gamma = saveTitle(
                "Catalog " + marker + " Gamma",
                true
        );

        addCategory(alpha, fiction, true);
        addCategory(alpha, history, false);
        addCategory(beta, fiction, true);
        addCategory(gamma, fiction, true);

        Specification<BookTitle> specification = Specification.allOf(
                BookTitleSpecifications.containsText(marker),
                BookTitleSpecifications.hasCategory(fiction.getId()),
                BookTitleSpecifications.hasActiveStatus(true)
        );

        PageRequest firstPageRequest = PageRequest.of(
                0,
                2,
                Sort.by("title").ascending()
        );

        Page<BookTitle> firstPage = bookTitleRepository.findAll(
                specification,
                firstPageRequest
        );

        Page<BookTitle> secondPage = bookTitleRepository.findAll(
                specification,
                firstPageRequest.next()
        );

        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());

        assertEquals(2, firstPage.getNumberOfElements());
        assertEquals(1, secondPage.getNumberOfElements());

        assertEquals(
                alpha.getId(),
                firstPage.getContent().get(0).getId()
        );

        assertEquals(
                beta.getId(),
                firstPage.getContent().get(1).getId()
        );

        assertEquals(
                gamma.getId(),
                secondPage.getContent().getFirst().getId()
        );
    }

    @Test
    void shouldTreatSearchWildcardsAsLiteralCharacters() {
        String marker = uniqueMarker();

        BookTitle withPercent = saveTitle(
                "Catalog " + marker + "% Special",
                true
        );

        saveTitle(
                "Catalog " + marker + " Ordinary",
                true
        );

        Page<BookTitle> result = bookTitleRepository.findAll(
                BookTitleSpecifications.containsText(marker + "%"),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                withPercent.getId(),
                result.getContent().getFirst().getId()
        );
    }

    private void assertSearchFindsId(
            String searchText,
            Long expectedId
    ) {
        Page<BookTitle> result = bookTitleRepository.findAll(
                BookTitleSpecifications.containsText(searchText),
                PageRequest.of(0, 20)
        );

        assertTrue(
                result.getContent().stream()
                        .anyMatch(title -> title.getId().equals(expectedId))
        );
    }

    private BookTitle saveTitle(
            String name,
            boolean active
    ) {
        BookTitle title = new BookTitle(
                name,
                "Catalog Test Author"
        );

        title.setActive(active);

        return bookTitleRepository.saveAndFlush(title);
    }

    private Category category(String code) {
        return categoryRepository.findByCode(code)
                .orElseThrow();
    }

    private void addCategory(
            BookTitle title,
            Category category,
            boolean primary
    ) {
        bookCategoryRepository.saveAndFlush(
                new BookCategory(title, category, primary)
        );
    }

    private String uniqueMarker() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
}
