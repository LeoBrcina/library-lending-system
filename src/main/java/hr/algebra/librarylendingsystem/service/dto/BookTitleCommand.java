package hr.algebra.librarylendingsystem.service.dto;

import java.util.List;

public record BookTitleCommand(
        String title,
        String authors,
        String isbn,
        String publisher,
        Integer publicationYear,
        String description,
        List<Long> categoryIds,
        Long primaryCategoryId
) {
    public BookTitleCommand {
        categoryIds = categoryIds == null
                ? List.of()
                : List.copyOf(categoryIds);
    }
}
