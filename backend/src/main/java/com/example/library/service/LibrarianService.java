package com.example.library.service;

import com.example.library.dto.AdminDtos;
import com.example.library.model.Loan;
import com.example.library.model.Reservation;
import com.example.library.model.ReservationStatus;
import com.example.library.repository.ReservationRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibrarianService {
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final LoanService loanService;

    public LibrarianService(ReservationRepository reservationRepository,
                            ReservationService reservationService,
                            LoanService loanService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.loanService = loanService;
    }

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminReservationResponse> reservations(int page, int size, String userQuery, String bookQuery, String status) {
        return reservationRepository.searchAdmin(userQuery, bookQuery, parseReservationStatus(status), PageRequest.of(page, size))
                .map(this::toReservationResponse);
    }

    @Transactional
    public AdminDtos.AdminReservationResponse issueReservation(Long reservationId) {
        Reservation reservation = reservationService.getReservationEntity(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled reservation cannot be issued");
        }
        if (reservation.getStatus() == ReservationStatus.FULFILLED && loanService.findOpenLoanForReservation(reservation.getUser().getId(), reservation.getBook().getId()) != null) {
            throw new IllegalStateException("Reservation already issued");
        }
        loanService.issueForReservation(reservation.getUser().getId(), reservation.getBook().getId());
        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationService.saveReservationEntity(reservation);
        Reservation updated = reservationService.getReservationEntity(reservationId);
        return toReservationResponse(updated);
    }

    @Transactional
    public AdminDtos.AdminReservationResponse returnReservation(Long reservationId) {
        Reservation reservation = reservationService.getReservationEntity(reservationId);
        Loan openLoan = loanService.findOpenLoanForReservation(reservation.getUser().getId(), reservation.getBook().getId());
        if (openLoan == null) {
            throw new IllegalStateException("No issued loan found for reservation");
        }
        loanService.returnLoan(openLoan.getId());
        return toReservationResponse(reservationService.getReservationEntity(reservationId));
    }

    private AdminDtos.AdminReservationResponse toReservationResponse(Reservation reservation) {
        Loan loan = loanService.findLatestLoanForReservation(reservation.getUser().getId(), reservation.getBook().getId());
        return new AdminDtos.AdminReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getBook().getId(),
                reservation.getBook().getTitle(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getNotifiedAt(),
                reservation.getExpiresAt(),
                reservation.getCancelledAt(),
                loan != null ? loan.getId() : null,
                loan != null ? loan.getStatus() : null,
                loan != null ? loan.getBorrowedAt() : null,
                loan != null ? loan.getDueDate() : null,
                loan != null ? loan.getReturnedAt() : null
        );
    }

    private ReservationStatus parseReservationStatus(String value) {
        if (value == null || value.isBlank()) return null;
        return ReservationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
