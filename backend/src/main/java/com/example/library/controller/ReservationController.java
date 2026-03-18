package com.example.library.controller;

import com.example.library.dto.ReservationDtos;
import com.example.library.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ReservationDtos.ReservationResponse create(@Valid @RequestBody ReservationDtos.CreateReservationRequest request) {
        return reservationService.create(request);
    }

    @PostMapping("/reservations/{id}/cancel")
    public ReservationDtos.ReservationResponse cancel(@PathVariable Long id, @RequestParam Long userId) {
        return reservationService.cancel(id, userId);
    }

    @GetMapping("/users/me/reservations")
    public List<ReservationDtos.ReservationResponse> myReservations() {
        return reservationService.getCurrentUserReservations();
    }

    @GetMapping("/users/{userId}/reservations")
    public List<ReservationDtos.ReservationResponse> userReservations(@PathVariable Long userId) {
        return reservationService.getUserReservations(userId);
    }
}
