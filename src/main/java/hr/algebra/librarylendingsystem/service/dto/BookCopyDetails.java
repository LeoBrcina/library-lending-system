package hr.algebra.librarylendingsystem.service.dto;

import java.time.LocalDate;

public record BookCopyDetails(
        Long id,
        Long bookTitleId,
        String inventoryCode,
        String copyCondition,
        String status,
        LocalDate acquiredAt
) {
}
