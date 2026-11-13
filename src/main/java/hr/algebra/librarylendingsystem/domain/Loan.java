package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookcopyid", nullable = false)
    private BookCopy bookCopy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memberid", nullable = false)
    private AppUser member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issuedbyid", nullable = false)
    private AppUser issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnedbyid")
    private AppUser returnedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservationid", unique = true)
    private Reservation reservation;

    @Column(name = "checkedoutat", nullable = false, updatable = false)
    private OffsetDateTime checkedOutAt;

    @Column(name = "duedate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returnedat")
    private OffsetDateTime returnedAt;

    @Column(name = "renewalcount", nullable = false)
    private int renewalCount = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Loan() {
        // Required by JPA.
    }

    public Loan(
            BookCopy bookCopy,
            AppUser member,
            AppUser issuedBy,
            LocalDate dueDate
    ) {
        if (bookCopy == null) {
            throw new IllegalArgumentException(
                    "Book copy is required."
            );
        }

        if (member == null) {
            throw new IllegalArgumentException(
                    "Member is required."
            );
        }

        if (issuedBy == null) {
            throw new IllegalArgumentException(
                    "Issuing librarian is required."
            );
        }

        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Due date is required."
            );
        }

        this.bookCopy = bookCopy;
        this.member = member;
        this.issuedBy = issuedBy;
        this.dueDate = dueDate;
    }

    @PrePersist
    protected void onCreate() {
        if (checkedOutAt == null) {
            checkedOutAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public AppUser getMember() {
        return member;
    }

    public AppUser getIssuedBy() {
        return issuedBy;
    }

    public AppUser getReturnedBy() {
        return returnedBy;
    }

    public void setReturnedBy(AppUser returnedBy) {
        this.returnedBy = returnedBy;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public OffsetDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Due date is required."
            );
        }

        this.dueDate = dueDate;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(OffsetDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(int renewalCount) {
        if (renewalCount < 0 || renewalCount > 2) {
            throw new IllegalArgumentException(
                    "Renewal count must be between 0 and 2."
            );
        }

        this.renewalCount = renewalCount;
    }

    public Long getVersion() {
        return version;
    }
}