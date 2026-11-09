package hr.algebra.librarylendingsystem.service.dto;

public record BookCatalogCategory(
        Long id,
        String name,
        boolean primary
) {
}
