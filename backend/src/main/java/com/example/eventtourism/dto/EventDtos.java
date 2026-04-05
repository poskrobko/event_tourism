package com.example.eventtourism.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EventDtos {
    public record EventCreateRequest(@NotBlank String title, @NotBlank String description, @NotNull LocalDate eventDate,
                                     @NotNull BigDecimal basePrice, @NotNull Long locationId) {}

    public record EventResponse(Long id, String title, String description, LocalDate eventDate,
                                BigDecimal basePrice, String city, String address, String mapUrl) {}

    public record EventProgramRequest(@NotNull LocalTime startTime, @NotBlank String activity) {}
    public record EventProgramResponse(Long id, LocalTime startTime, String activity) {}

    public record TicketRequest(@NotBlank String category, @NotNull BigDecimal price, @Min(1) Integer quantity) {}
    public record TicketResponse(Long id, Long eventId, String eventTitle, String category, BigDecimal price, Integer quantity) {}

    public record EventDetailsResponse(EventResponse event, List<EventProgramResponse> program, List<TicketResponse> tickets,
                                       String calendarMockUrl) {}
}
