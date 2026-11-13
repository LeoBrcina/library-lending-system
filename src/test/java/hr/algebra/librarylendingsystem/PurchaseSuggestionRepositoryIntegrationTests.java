package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.AiProcessingStatus;
import hr.algebra.librarylendingsystem.domain.AiSuggestionResult;
import hr.algebra.librarylendingsystem.domain.AppUser;
import hr.algebra.librarylendingsystem.domain.PurchaseSuggestion;
import hr.algebra.librarylendingsystem.domain.PurchaseSuggestionStatus;
import hr.algebra.librarylendingsystem.repository.AiSuggestionResultRepository;
import hr.algebra.librarylendingsystem.repository.AppUserRepository;
import hr.algebra.librarylendingsystem.repository.PurchaseSuggestionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PurchaseSuggestionRepositoryIntegrationTests {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PurchaseSuggestionRepository purchaseSuggestionRepository;

    @Autowired
    private AiSuggestionResultRepository aiSuggestionResultRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrievePurchaseSuggestion() {
        AppUser member = createUser();

        PurchaseSuggestion suggestion = new PurchaseSuggestion(
                member,
                "Clean Code",
                "Please consider purchasing this book."
        );

        suggestion = purchaseSuggestionRepository.saveAndFlush(suggestion);

        Long suggestionId = suggestion.getId();

        entityManager.clear();

        PurchaseSuggestion savedSuggestion = purchaseSuggestionRepository
                .findById(suggestionId)
                .orElseThrow();

        assertEquals("Clean Code", savedSuggestion.getSuggestedTitle());

        assertEquals(
                "Please consider purchasing this book.",
                savedSuggestion.getDescription()
        );

        assertEquals(
                member.getId(),
                savedSuggestion.getMember().getId()
        );

        assertEquals(
                PurchaseSuggestionStatus.PENDING,
                savedSuggestion.getStatus()
        );

        assertNotNull(savedSuggestion.getSubmittedAt());
        assertNull(savedSuggestion.getReviewedBy());
        assertNull(savedSuggestion.getReviewedAt());
    }

    @Test
    void shouldFindPurchaseSuggestionsByMember() {
        AppUser member = createUser();
        AppUser anotherMember = createUser();

        PurchaseSuggestion first = createSuggestion(member);
        PurchaseSuggestion second = createSuggestion(member);

        createSuggestion(anotherMember);

        entityManager.clear();

        List<PurchaseSuggestion> suggestions =
                purchaseSuggestionRepository
                        .findByMember_IdOrderBySubmittedAtDesc(
                                member.getId()
                        );

        assertEquals(2, suggestions.size());

        assertTrue(
                suggestions.stream()
                        .anyMatch(s -> s.getId().equals(first.getId()))
        );

        assertTrue(
                suggestions.stream()
                        .anyMatch(s -> s.getId().equals(second.getId()))
        );
    }

    @Test
    void shouldFindPendingPurchaseSuggestions() {
        PurchaseSuggestion pending = createSuggestion(createUser());

        PurchaseSuggestion approved = createSuggestion(createUser());
        approved.setStatus(PurchaseSuggestionStatus.APPROVED);

        purchaseSuggestionRepository.saveAndFlush(approved);

        entityManager.clear();

        List<PurchaseSuggestion> pendingSuggestions =
                purchaseSuggestionRepository
                        .findByStatusOrderBySubmittedAtAscIdAsc(
                                PurchaseSuggestionStatus.PENDING
                        );

        assertTrue(
                pendingSuggestions.stream()
                        .anyMatch(s -> s.getId().equals(pending.getId()))
        );

        assertFalse(
                pendingSuggestions.stream()
                        .anyMatch(s -> s.getId().equals(approved.getId()))
        );
    }

    @Test
    void shouldPersistLibrarianReviewInformation() {
        PurchaseSuggestion suggestion = createSuggestion(createUser());
        AppUser reviewer = createUser();

        OffsetDateTime reviewedAt = OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MICROS);

        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(reviewedAt);
        suggestion.setStatus(PurchaseSuggestionStatus.APPROVED);

        purchaseSuggestionRepository.saveAndFlush(suggestion);

        Long suggestionId = suggestion.getId();

        entityManager.clear();

        PurchaseSuggestion reviewedSuggestion =
                purchaseSuggestionRepository
                        .findById(suggestionId)
                        .orElseThrow();

        assertEquals(
                PurchaseSuggestionStatus.APPROVED,
                reviewedSuggestion.getStatus()
        );

        assertEquals(
                reviewer.getId(),
                reviewedSuggestion.getReviewedBy().getId()
        );

        assertEquals(
                reviewedAt.toInstant(),
                reviewedSuggestion.getReviewedAt().toInstant()
        );

        List<PurchaseSuggestion> reviewed =
                purchaseSuggestionRepository
                        .findByReviewedBy_IdOrderByReviewedAtDesc(
                                reviewer.getId()
                        );

        assertTrue(
                reviewed.stream()
                        .anyMatch(s -> s.getId().equals(suggestionId))
        );
    }

    @Test
    void shouldSaveAndRetrievePendingAiResult() {
        PurchaseSuggestion suggestion = createSuggestion(createUser());

        AiSuggestionResult result = new AiSuggestionResult(suggestion);

        result = aiSuggestionResultRepository.saveAndFlush(result);

        Long resultId = result.getId();

        entityManager.clear();

        AiSuggestionResult savedResult =
                aiSuggestionResultRepository
                        .findById(resultId)
                        .orElseThrow();

        assertEquals(
                suggestion.getId(),
                savedResult.getPurchaseSuggestion().getId()
        );

        assertEquals(
                AiProcessingStatus.PENDING,
                savedResult.getProcessingStatus()
        );

        assertNull(savedResult.getSuggestedCategories());
        assertNull(savedResult.getDuplicateCandidates());
        assertNotNull(savedResult.getCreatedAt());
    }

    @Test
    void shouldPersistAndRetrieveJsonbRecommendations() {
        PurchaseSuggestion suggestion = createSuggestion(createUser());

        List<Map<String, Object>> categories = List.of(
                Map.of(
                        "code", "COMPUTING",
                        "confidence", 0.95
                ),
                Map.of(
                        "code", "TECHNOLOGY",
                        "confidence", 0.82
                )
        );

        List<Map<String, Object>> duplicates = List.of(
                Map.of(
                        "titleId", 42,
                        "title", "Clean Code",
                        "similarity", 0.91
                )
        );

        AiSuggestionResult result = new AiSuggestionResult(suggestion);

        result.setSuggestedCategories(categories);
        result.setDuplicateCandidates(duplicates);
        result.setProcessingStatus(AiProcessingStatus.COMPLETED);

        result = aiSuggestionResultRepository.saveAndFlush(result);

        Long resultId = result.getId();

        entityManager.clear();

        AiSuggestionResult savedResult =
                aiSuggestionResultRepository
                        .findById(resultId)
                        .orElseThrow();

        assertEquals(
                AiProcessingStatus.COMPLETED,
                savedResult.getProcessingStatus()
        );

        assertEquals(2, savedResult.getSuggestedCategories().size());
        assertEquals(1, savedResult.getDuplicateCandidates().size());

        assertEquals(
                "COMPUTING",
                savedResult.getSuggestedCategories().get(0).get("code")
        );

        assertEquals(
                "TECHNOLOGY",
                savedResult.getSuggestedCategories().get(1).get("code")
        );

        assertEquals(
                0.95,
                ((Number) savedResult.getSuggestedCategories()
                        .get(0).get("confidence")).doubleValue(),
                0.0001
        );

        assertEquals(
                "Clean Code",
                savedResult.getDuplicateCandidates().get(0).get("title")
        );

        assertEquals(
                42,
                ((Number) savedResult.getDuplicateCandidates()
                        .get(0).get("titleId")).intValue()
        );
    }

    @Test
    void shouldPreserveMultipleAiResultsForSameSuggestion() {
        PurchaseSuggestion suggestion = createSuggestion(createUser());

        AiSuggestionResult first = new AiSuggestionResult(suggestion);
        first.setProcessingStatus(AiProcessingStatus.FAILED);

        first = aiSuggestionResultRepository.saveAndFlush(first);

        AiSuggestionResult second = new AiSuggestionResult(suggestion);
        second.setProcessingStatus(AiProcessingStatus.COMPLETED);

        second = aiSuggestionResultRepository.saveAndFlush(second);

        Long firstId = first.getId();
        Long secondId = second.getId();

        entityManager.clear();

        List<AiSuggestionResult> results =
                aiSuggestionResultRepository
                        .findByPurchaseSuggestion_IdOrderByCreatedAtDescIdDesc(
                                suggestion.getId()
                        );

        assertEquals(2, results.size());

        assertEquals(secondId, results.get(0).getId());
        assertEquals(firstId, results.get(1).getId());

        AiSuggestionResult latest =
                aiSuggestionResultRepository
                        .findFirstByPurchaseSuggestion_IdOrderByCreatedAtDescIdDesc(
                                suggestion.getId()
                        )
                        .orElseThrow();

        assertEquals(secondId, latest.getId());
    }

    @Test
    void shouldFindAiResultsByProcessingStatus() {
        PurchaseSuggestion suggestion = createSuggestion(createUser());

        AiSuggestionResult pending = new AiSuggestionResult(suggestion);

        AiSuggestionResult failed = new AiSuggestionResult(suggestion);
        failed.setProcessingStatus(AiProcessingStatus.FAILED);

        pending = aiSuggestionResultRepository.saveAndFlush(pending);
        failed = aiSuggestionResultRepository.saveAndFlush(failed);

        Long pendingId = pending.getId();
        Long failedId = failed.getId();

        entityManager.clear();

        List<AiSuggestionResult> pendingResults =
                aiSuggestionResultRepository.findByProcessingStatus(
                        AiProcessingStatus.PENDING
                );

        List<AiSuggestionResult> failedResults =
                aiSuggestionResultRepository.findByProcessingStatus(
                        AiProcessingStatus.FAILED
                );

        assertTrue(
                pendingResults.stream()
                        .anyMatch(r -> r.getId().equals(pendingId))
        );

        assertFalse(
                pendingResults.stream()
                        .anyMatch(r -> r.getId().equals(failedId))
        );

        assertTrue(
                failedResults.stream()
                        .anyMatch(r -> r.getId().equals(failedId))
        );
    }

    private AppUser createUser() {
        return appUserRepository.saveAndFlush(
                new AppUser(
                        "test-" + UUID.randomUUID() + "@example.com",
                        "test-password-hash",
                        "Test",
                        "User"
                )
        );
    }

    private PurchaseSuggestion createSuggestion(AppUser member) {
        return purchaseSuggestionRepository.saveAndFlush(
                new PurchaseSuggestion(
                        member,
                        "Suggested Book " + UUID.randomUUID(),
                        "Integration test purchase suggestion."
                )
        );
    }
}