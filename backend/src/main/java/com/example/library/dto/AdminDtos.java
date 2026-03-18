package com.example.library.dto;

import com.example.library.model.LoanStatus;
import com.example.library.model.ReservationStatus;
import com.example.library.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public class AdminDtos {
    public record AdminUserResponse(
            Long id,
            String email,
            String nickname,
            Set<Role> roles
    ) {}

    public record AdminUserUpdateRequest(
            @Email @NotBlank String email,
            @Size(min = 2, max = 50) String nickname,
            Set<Role> roles
    ) {}

    public record AdminLoanResponse(
            Long id,
            Long userId,
            String userEmail,
            Long bookId,
            String bookTitle,
            LoanStatus status,
            LocalDate borrowedAt,
            LocalDate dueDate,
            LocalDate returnedAt
    ) {}

    public record AdminReservationResponse(
            Long id,
            Long userId,
            String userEmail,
            Long bookId,
            String bookTitle,
            ReservationStatus status,
            Instant createdAt,
            Instant notifiedAt,
            Instant expiresAt,
            Instant cancelledAt,
            Long loanId,
            LoanStatus loanStatus,
            LocalDate borrowedAt,
            LocalDate dueDate,
            LocalDate returnedAt
    ) {}

    public record AdminRatingResponse(
            Long id,
            Long userId,
            String userEmail,
            Long bookId,
            String bookTitle,
            Integer score
    ) {}

    public record AdminReviewResponse(
            Long id,
            Long userId,
            String userEmail,
            Long bookId,
            String bookTitle,
            String text,
            Instant createdAt
    ) {}

    public record AdminRecommendationProfileResponse(
            Long id,
            Long userId,
            String userEmail,
            String preferredGenresCsv,
            String favoriteAuthorsCsv
    ) {}

    public record InviteLibrarianRequest(
            @Email @NotBlank String email,
            @Size(min = 2, max = 50) String nickname
    ) {}

    public record InviteLibrarianResponse(
            Long userId,
            String email,
            String temporaryPassword,
            String message
    ) {}
}
