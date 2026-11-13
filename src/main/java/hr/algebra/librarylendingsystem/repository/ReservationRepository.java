package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.Reservation;
import hr.algebra.librarylendingsystem.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBookTitle_IdAndStatusOrderByRequestedAtAscIdAsc(
            Long bookTitleId,
            ReservationStatus status
    );

    boolean existsByMember_IdAndBookTitle_IdAndStatusIn(
            Long memberId,
            Long bookTitleId,
            Collection<ReservationStatus> statuses
    );

    List<Reservation> findByStatusAndPickupDeadlineBefore(
            ReservationStatus status,
            OffsetDateTime deadline
    );

    List<Reservation> findByMember_IdOrderByRequestedAtDesc(
            Long memberId
    );
}