package com.example.library.repository;

import com.example.library.model.Reservation;
import com.example.library.model.ReservationStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Reservation> findFirstByBookIdAndStatusOrderByCreatedAtAsc(Long bookId, ReservationStatus status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<ReservationStatus> statuses);

    @Query("""
            select r from Reservation r
            where (:userQuery is null or :userQuery = ''
                   or lower(r.user.email) like lower(concat('%', :userQuery, '%')))
              and (:bookQuery is null or :bookQuery = ''
                   or lower(r.book.title) like lower(concat('%', :bookQuery, '%')))
              and (:status is null or r.status = :status)
            """)
    Page<Reservation> searchAdmin(@Param("userQuery") String userQuery,
                                  @Param("bookQuery") String bookQuery,
                                  @Param("status") ReservationStatus status,
                                  Pageable pageable);
}
