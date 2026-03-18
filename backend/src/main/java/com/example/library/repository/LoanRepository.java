package com.example.library.repository;

import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserIdOrderByBorrowedAtDesc(Long userId);
    long countByUserIdAndStatus(Long userId, LoanStatus status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<LoanStatus> statuses);
    Optional<Loan> findFirstByUserIdAndBookIdAndStatusInOrderByBorrowedAtDesc(Long userId, Long bookId, Collection<LoanStatus> statuses);
    Optional<Loan> findFirstByUserIdAndBookIdOrderByBorrowedAtDesc(Long userId, Long bookId);

    @Query("""
            select l from Loan l
            where (:userQuery is null or :userQuery = ''
                    or lower(l.user.email) like lower(concat('%', :userQuery, '%'))
                    or lower(coalesce(l.user.nickname, '')) like lower(concat('%', :userQuery, '%')))
              and (:bookQuery is null or :bookQuery = ''
                    or lower(l.book.title) like lower(concat('%', :bookQuery, '%'))
                    or lower(l.book.author) like lower(concat('%', :bookQuery, '%')))
              and (:status is null or l.status = :status)
            """)
    Page<Loan> searchAdmin(@Param("userQuery") String userQuery,
                           @Param("bookQuery") String bookQuery,
                           @Param("status") LoanStatus status,
                           Pageable pageable);
}
