package hr.algebra.librarylendingsystem.service.dto;

import java.time.LocalDate;

public record BookCopyCommand(
        String inventoryCode,
        String copyCondition,
        LocalDate acquiredAt
) {
}
