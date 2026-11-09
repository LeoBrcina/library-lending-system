package hr.algebra.librarylendingsystem.service.dto;

public record BookCatalogSearchCriteria(
        String searchText,
        Long categoryId,
        Boolean active,
        int page,
        int size,
        String sortBy,
        boolean ascending
) {
}
