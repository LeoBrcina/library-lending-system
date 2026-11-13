package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.PurchaseSuggestion;
import hr.algebra.librarylendingsystem.domain.PurchaseSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseSuggestionRepository
        extends JpaRepository<PurchaseSuggestion, Long> {

    List<PurchaseSuggestion> findByMember_IdOrderBySubmittedAtDesc(
            Long memberId
    );

    List<PurchaseSuggestion> findByStatusOrderBySubmittedAtAscIdAsc(
            PurchaseSuggestionStatus status
    );

    List<PurchaseSuggestion> findByReviewedBy_IdOrderByReviewedAtDesc(
            Long reviewerId
    );
}