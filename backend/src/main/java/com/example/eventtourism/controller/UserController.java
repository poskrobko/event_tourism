package com.example.eventtourism.controller;

import com.example.eventtourism.dto.OrderDtos;
import com.example.eventtourism.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final OrderService orderService;

    public UserController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/tickets/buy")
    public OrderDtos.OrderResponse buyTicket(@Valid @RequestBody OrderDtos.BuyTicketRequest request, Authentication authentication) {
        return orderService.buy(authentication.getName(), request);
    }

    @GetMapping("/orders")
    public List<OrderDtos.OrderResponse> myOrders(Authentication authentication) {
        return orderService.myOrders(authentication.getName());
    }

    @GetMapping("/tickets")
    public List<OrderDtos.OrderResponse> myTickets(Authentication authentication) {
        return orderService.myOrders(authentication.getName());
    }
}
