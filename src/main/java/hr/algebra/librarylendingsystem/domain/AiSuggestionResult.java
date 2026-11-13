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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "aisuggestionresults")
public class AiSuggestionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchasesuggestionid", nullable = false)
    private PurchaseSuggestion purchaseSuggestion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "suggestedcategories", columnDefinition = "jsonb")
    private List<Map<String, Object>> suggestedCategories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "duplicatecandidates", columnDefinition = "jsonb")
    private List<Map<String, Object>> duplicateCandidates;

    @Enumerated(EnumType.STRING)
    @Column(name = "processingstatus", nullable = false, length = 30)
    private AiProcessingStatus processingStatus = AiProcessingStatus.PENDING;

    @Column(name = "createdat", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AiSuggestionResult() {
        // Required by JPA.
    }

    public AiSuggestionResult(PurchaseSuggestion purchaseSuggestion) {
        if (purchaseSuggestion == null) {
            throw new IllegalArgumentException(
                    "Purchase suggestion is required."
            );
        }

        this.purchaseSuggestion = purchaseSuggestion;
        this.processingStatus = AiProcessingStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public PurchaseSuggestion getPurchaseSuggestion() {
        return purchaseSuggestion;
    }

    public List<Map<String, Object>> getSuggestedCategories() {
        return suggestedCategories;
    }

    public void setSuggestedCategories(
            List<Map<String, Object>> suggestedCategories
    ) {
        this.suggestedCategories = suggestedCategories;
    }

    public List<Map<String, Object>> getDuplicateCandidates() {
        return duplicateCandidates;
    }

    public void setDuplicateCandidates(
            List<Map<String, Object>> duplicateCandidates
    ) {
        this.duplicateCandidates = duplicateCandidates;
    }

    public AiProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(AiProcessingStatus processingStatus) {
        if (processingStatus == null) {
            throw new IllegalArgumentException(
                    "AI processing status is required."
            );
        }

        this.processingStatus = processingStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}