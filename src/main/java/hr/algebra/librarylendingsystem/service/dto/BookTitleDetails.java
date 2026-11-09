package hr.algebra.librarylendingsystem.service.dto;

import java.util.List;

public record BookTitleDetails(
        Long id,
        String title,
        String authors,
        String isbn,
        String publisher,
        Integer publicationYear,
        String description,
        boolean active,
        List<Long> categoryIds,
        Long primaryCategoryId
) {
    public BookTitleDetails {
        categoryIds = List.copyOf(categoryIds);
    }
}
