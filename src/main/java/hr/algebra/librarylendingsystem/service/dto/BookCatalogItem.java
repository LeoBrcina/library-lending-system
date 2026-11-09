package hr.algebra.librarylendingsystem.service.dto;

import java.util.List;

public record BookCatalogItem(
        Long id,
        String title,
        String authors,
        String isbn,
        boolean active,
        List<BookCatalogCategory> categories,
        long totalCopies,
        long availableCopies
) {
    public BookCatalogItem {
        categories = List.copyOf(categories);
    }
}
