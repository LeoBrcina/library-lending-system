package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booktitleid", nullable = false)
    private BookTitle bookTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memberid", nullable = false)
    private AppUser member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocatedcopyid")
    private BookCopy allocatedCopy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.WAITING;

    @Column(name = "requestedat", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "readyat")
    private OffsetDateTime readyAt;

    @Column(name = "pickupdeadline")
    private OffsetDateTime pickupDeadline;

    @Column(name = "fulfilledat")
    private OffsetDateTime fulfilledAt;

    protected Reservation() {
        // Required by JPA.
    }

    public Reservation(
            BookTitle bookTitle,
            AppUser member
    ) {
        if (bookTitle == null) {
            throw new IllegalArgumentException(
                    "Book title is required."
            );
        }

        if (member == null) {
            throw new IllegalArgumentException(
                    "Member is required."
            );
        }

        this.bookTitle = bookTitle;
        this.member = member;
        this.status = ReservationStatus.WAITING;
    }

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public AppUser getMember() {
        return member;
    }

    public BookCopy getAllocatedCopy() {
        return allocatedCopy;
    }

    public void setAllocatedCopy(BookCopy allocatedCopy) {
        this.allocatedCopy = allocatedCopy;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Reservation status is required."
            );
        }

        this.status = status;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public OffsetDateTime getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(OffsetDateTime readyAt) {
        this.readyAt = readyAt;
    }

    public OffsetDateTime getPickupDeadline() {
        return pickupDeadline;
    }

    public void setPickupDeadline(OffsetDateTime pickupDeadline) {
        this.pickupDeadline = pickupDeadline;
    }

    public OffsetDateTime getFulfilledAt() {
        return fulfilledAt;
    }

    public void setFulfilledAt(OffsetDateTime fulfilledAt) {
        this.fulfilledAt = fulfilledAt;
    }
}