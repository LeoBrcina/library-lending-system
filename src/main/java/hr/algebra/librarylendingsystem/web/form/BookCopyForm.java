package hr.algebra.librarylendingsystem.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class BookCopyForm {

    @NotBlank(message = "Inventory code is required.")
    @Size(
            max = 100,
            message = "Inventory code must not exceed 100 characters."
    )
    private String inventoryCode;

    @NotBlank(message = "Select a physical condition.")
    @Pattern(
            regexp = "NEW|GOOD|FAIR|POOR|DAMAGED",
            message = "Select a valid physical condition."
    )
    private String copyCondition;

    @PastOrPresent(
            message = "Acquisition date cannot be in the future."
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate acquiredAt;

    public BookCopyForm() {
    }

    public String getInventoryCode() {
        return inventoryCode;
    }

    public void setInventoryCode(String inventoryCode) {
        this.inventoryCode = inventoryCode;
    }

    public String getCopyCondition() {
        return copyCondition;
    }

    public void setCopyCondition(String copyCondition) {
        this.copyCondition = copyCondition;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
    }
}
