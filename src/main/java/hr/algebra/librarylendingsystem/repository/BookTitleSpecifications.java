package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class BookTitleSpecifications {

    private BookTitleSpecifications() {
    }

    public static Specification<BookTitle> containsText(
            String searchText
    ) {
        if (searchText == null || searchText.isBlank()) {
            return (root, query, builder) -> builder.conjunction();
        }

        String normalized = searchText.trim()
                .toLowerCase(Locale.ROOT);

        String escaped = normalized
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        String pattern = "%" + escaped + "%";

        return (root, query, builder) -> builder.or(
                builder.like(
                        builder.lower(root.get("title")),
                        pattern,
                        '\\'
                ),
                builder.like(
                        builder.lower(root.get("authors")),
                        pattern,
                        '\\'
                ),
                builder.like(
                        builder.lower(root.get("isbn")),
                        pattern,
                        '\\'
                )
        );
    }

    public static Specification<BookTitle> hasCategory(
            Long categoryId
    ) {
        if (categoryId == null) {
            return (root, query, builder) -> builder.conjunction();
        }

        return (root, query, builder) -> {
            if (query == null) {
                throw new IllegalStateException(
                        "Category filtering requires a select query."
                );
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<BookCategory> association =
                    subquery.from(BookCategory.class);

            subquery.select(
                    association.get("bookTitle").get("id")
            );

            subquery.where(
                    builder.equal(
                            association.get("bookTitle").get("id"),
                            root.get("id")
                    ),
                    builder.equal(
                            association.get("category").get("id"),
                            categoryId
                    )
            );

            return builder.exists(subquery);
        };
    }

    public static Specification<BookTitle> hasActiveStatus(
            Boolean active
    ) {
        if (active == null) {
            return (root, query, builder) -> builder.conjunction();
        }

        return (root, query, builder) ->
                builder.equal(root.get("active"), active);
    }
}
