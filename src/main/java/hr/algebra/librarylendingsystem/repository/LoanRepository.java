package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByBookCopy_IdAndReturnedAtIsNull(
            Long bookCopyId
    );

    boolean existsByBookCopy_IdAndReturnedAtIsNull(
            Long bookCopyId
    );

    List<Loan> findByMember_IdAndReturnedAtIsNull(
            Long memberId
    );

    long countByMember_IdAndReturnedAtIsNull(
            Long memberId
    );

    List<Loan> findByReturnedAtIsNullAndDueDateBefore(
            LocalDate date
    );

    List<Loan> findByMember_IdOrderByCheckedOutAtDesc(
            Long memberId
    );
}