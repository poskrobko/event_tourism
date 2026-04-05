package com.example.eventtourism.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDtos {
    public record BuyTicketRequest(@NotNull Long ticketId, @Min(1) Integer amount) {}
    public record OrderResponse(Long id, Long ticketId, String eventTitle, Integer amount, BigDecimal totalPrice,
                                String status, LocalDateTime createdAt) {}
}
