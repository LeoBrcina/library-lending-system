package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.FineAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FineAccrualRepository
        extends JpaRepository<FineAccrual, Long> {

    List<FineAccrual> findByLoan_IdOrderByAccrualDateAsc(
            Long loanId
    );

    boolean existsByLoan_IdAndAccrualDate(
            Long loanId,
            LocalDate accrualDate
    );

    List<FineAccrual> findByLoan_Member_Id(
            Long memberId
    );
}