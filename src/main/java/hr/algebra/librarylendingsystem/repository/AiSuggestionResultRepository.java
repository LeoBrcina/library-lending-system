package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.AiProcessingStatus;
import hr.algebra.librarylendingsystem.domain.AiSuggestionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiSuggestionResultRepository
        extends JpaRepository<AiSuggestionResult, Long> {

    List<AiSuggestionResult> findByPurchaseSuggestion_IdOrderByCreatedAtDescIdDesc(
            Long purchaseSuggestionId
    );

    Optional<AiSuggestionResult> findFirstByPurchaseSuggestion_IdOrderByCreatedAtDescIdDesc(
            Long purchaseSuggestionId
    );

    List<AiSuggestionResult> findByProcessingStatus(
            AiProcessingStatus processingStatus
    );
}