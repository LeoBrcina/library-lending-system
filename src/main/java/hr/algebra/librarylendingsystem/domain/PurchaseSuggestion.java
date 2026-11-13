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
@Table(name = "purchasesuggestions")
public class PurchaseSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memberid", nullable = false)
    private AppUser member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedbyid")
    private AppUser reviewedBy;

    @Column(name = "suggestedtitle", nullable = false, length = 255)
    private String suggestedTitle;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PurchaseSuggestionStatus status = PurchaseSuggestionStatus.PENDING;

    @Column(name = "submittedat", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "reviewedat")
    private OffsetDateTime reviewedAt;

    protected PurchaseSuggestion() {
        // Required by JPA.
    }

    public PurchaseSuggestion(
            AppUser member,
            String suggestedTitle,
            String description
    ) {
        if (member == null) {
            throw new IllegalArgumentException(
                    "Member is required."
            );
        }

        if (suggestedTitle == null || suggestedTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "Suggested title is required."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description is required."
            );
        }

        this.member = member;
        this.suggestedTitle = suggestedTitle.trim();
        this.description = description.trim();
        this.status = PurchaseSuggestionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getMember() {
        return member;
    }

    public AppUser getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(AppUser reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getSuggestedTitle() {
        return suggestedTitle;
    }

    public String getDescription() {
        return description;
    }

    public PurchaseSuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseSuggestionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Purchase suggestion status is required."
            );
        }

        this.status = status;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}