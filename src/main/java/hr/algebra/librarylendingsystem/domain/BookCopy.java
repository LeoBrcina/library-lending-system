package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "bookcopies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booktitleid", nullable = false)
    private BookTitle bookTitle;

    @Column(name = "inventorycode", nullable = false, length = 100)
    private String inventoryCode;

    @Column(name = "copycondition", nullable = false, length = 30)
    private String copyCondition;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "acquiredat")
    private LocalDate acquiredAt;

    protected BookCopy() {
        // Required by JPA.
    }

    public BookCopy(
            String inventoryCode,
            String copyCondition,
            String status
    ) {
        this.inventoryCode = inventoryCode;
        this.copyCondition = copyCondition;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    void setBookTitle(BookTitle bookTitle) {
        this.bookTitle = bookTitle;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
    }
}
