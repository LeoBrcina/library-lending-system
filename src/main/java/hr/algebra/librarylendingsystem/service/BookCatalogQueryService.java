package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleSpecifications;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogCategory;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogItem;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookCatalogQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "title",
            "authors",
            "isbn",
            "publicationYear"
    );

    private final BookTitleRepository bookTitleRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final BookCopyRepository bookCopyRepository;

    public BookCatalogQueryService(
            BookTitleRepository bookTitleRepository,
            BookCategoryRepository bookCategoryRepository,
            BookCopyRepository bookCopyRepository
    ) {
        this.bookTitleRepository = bookTitleRepository;
        this.bookCategoryRepository = bookCategoryRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookCatalogItem> search(
            BookCatalogSearchCriteria criteria
    ) {
        validateSearchCriteria(criteria);

        String sortField = criteria.sortBy();

        if (sortField == null || sortField.isBlank()) {
            sortField = "title";
        }

        Sort.Direction direction = criteria.ascending()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortField);

        if (!"id".equals(sortField)) {
            sort = sort.and(Sort.by("id").ascending());
        }

        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                sort
        );

        Specification<BookTitle> specification = Specification.allOf(
                BookTitleSpecifications.containsText(
                        criteria.searchText()
                ),
                BookTitleSpecifications.hasCategory(
                        criteria.categoryId()
                ),
                BookTitleSpecifications.hasActiveStatus(
                        criteria.active()
                )
        );

        Page<BookTitle> titlePage = bookTitleRepository.findAll(
                specification,
                pageable
        );

        if (titlePage.isEmpty()) {
            return new PageImpl<>(
                    List.of(),
                    pageable,
                    titlePage.getTotalElements()
            );
        }

        List<Long> titleIds = titlePage.getContent()
                .stream()
                .map(BookTitle::getId)
                .toList();

        Map<Long, List<BookCatalogCategory>> categoriesByTitle =
                loadCatalogCategories(titleIds);

        Map<Long, BookCopyRepository.BookCopyCountView> copyCountsByTitle =
                loadCopyCounts(titleIds);

        return titlePage.map(title -> {
            Long titleId = title.getId();

            List<BookCatalogCategory> categories =
                    categoriesByTitle.getOrDefault(
                            titleId,
                            List.of()
                    );

            BookCopyRepository.BookCopyCountView counts =
                    copyCountsByTitle.get(titleId);

            long totalCopies = counts == null
                    ? 0
                    : counts.getTotalCopies();

            long availableCopies = counts == null
                    ? 0
                    : counts.getAvailableCopies();

            return new BookCatalogItem(
                    titleId,
                    title.getTitle(),
                    title.getAuthors(),
                    title.getIsbn(),
                    title.isActive(),
                    categories,
                    totalCopies,
                    availableCopies
            );
        });
    }

    private void validateSearchCriteria(
            BookCatalogSearchCriteria criteria
    ) {
        if (criteria == null) {
            throw new IllegalArgumentException(
                    "Catalog search criteria are required."
            );
        }

        if (criteria.page() < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (criteria.size() < 1
                || criteria.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE + "."
            );
        }

        if (criteria.categoryId() != null
                && criteria.categoryId() <= 0) {
            throw new IllegalArgumentException(
                    "Category ID must be positive."
            );
        }

        String sortBy = criteria.sortBy();

        if (sortBy != null
                && !sortBy.isBlank()
                && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported catalog sort field: " + sortBy
            );
        }
    }

    private Map<Long, List<BookCatalogCategory>> loadCatalogCategories(
            List<Long> titleIds
    ) {
        List<BookCategory> associations =
                bookCategoryRepository.findByBookTitle_IdIn(titleIds);

        Map<Long, List<BookCatalogCategory>> result = new HashMap<>();

        for (BookCategory association : associations) {
            Long titleId = association.getBookTitle().getId();
            Category category = association.getCategory();

            BookCatalogCategory catalogCategory =
                    new BookCatalogCategory(
                            category.getId(),
                            category.getName(),
                            association.isPrimary()
                    );

            result.computeIfAbsent(
                    titleId,
                    ignored -> new ArrayList<>()
            ).add(catalogCategory);
        }

        Comparator<BookCatalogCategory> categoryOrder =
                Comparator.comparing(
                        BookCatalogCategory::primary
                ).reversed().thenComparing(
                        BookCatalogCategory::name
                ).thenComparing(
                        BookCatalogCategory::id
                );

        for (List<BookCatalogCategory> categories : result.values()) {
            categories.sort(categoryOrder);
        }

        return result;
    }

    private Map<Long, BookCopyRepository.BookCopyCountView> loadCopyCounts(
            List<Long> titleIds
    ) {
        Map<Long, BookCopyRepository.BookCopyCountView> result =
                new HashMap<>();

        for (BookCopyRepository.BookCopyCountView counts
                : bookCopyRepository.countCopiesByBookTitleIds(titleIds)) {
            result.put(counts.getBookTitleId(), counts);
        }

        return result;
    }
}
