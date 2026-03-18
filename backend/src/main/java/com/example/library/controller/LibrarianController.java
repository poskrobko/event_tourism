package com.example.library.controller;

import com.example.library.dto.AdminDtos;
import com.example.library.service.LibrarianService;
import com.example.library.dto.LoanDtos;
import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import com.example.library.service.LoanService;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/librarian")
public class LibrarianController {
    private final LibrarianService librarianService;
    private final LoanService loanService;

    public LibrarianController(LibrarianService librarianService, LoanService loanService) {
        this.librarianService = librarianService;
        this.loanService = loanService;
    }

    @GetMapping("/reservations")
    public Page<AdminDtos.AdminReservationResponse> reservations(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 @RequestParam(required = false) String userQuery,
                                                                 @RequestParam(required = false) String bookQuery,
                                                                 @RequestParam(required = false) String status) {
        return librarianService.reservations(page, size, userQuery, bookQuery, status);
    }

    @PostMapping("/reservations/{id}/issue")
    public AdminDtos.AdminReservationResponse issue(@PathVariable Long id) {
        return librarianService.issueReservation(id);
    }
    @PostMapping("/loans/{id}/issue")
    public LoanDtos.LoanResponse issueLoan(@PathVariable Long id) {
        return loanService.issueLoan(id);
    }

    @PostMapping("/loans/{id}/return")
    public LoanDtos.LoanResponse markReturned(@PathVariable Long id) {
        return loanService.markReturned(id);
    }

    private AdminDtos.AdminLoanResponse toLoanResponse(Loan loan) {
        return new AdminDtos.AdminLoanResponse(
                loan.getId(),
                loan.getUser().getId(),
                loan.getUser().getEmail(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getStatus(),
                loan.getBorrowedAt(),
                loan.getDueDate(),
                loan.getReturnedAt()
        );
    }

    @PostMapping("/reservations/{id}/return")
    public AdminDtos.AdminReservationResponse markReturned(@PathVariable Long id) {
        return librarianService.returnReservation(id);
    }
}
