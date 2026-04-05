package com.example.eventtourism.service;

import com.example.eventtourism.dto.OrderDtos;
import com.example.eventtourism.entity.*;
import com.example.eventtourism.exception.BadRequestException;
import com.example.eventtourism.exception.NotFoundException;
import com.example.eventtourism.repository.OrderRepository;
import com.example.eventtourism.repository.PaymentRepository;
import com.example.eventtourism.repository.UserRepository;
import com.example.eventtourism.service.pricing.PricingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PricingStrategy regularPricing;
    private final PricingStrategy groupPricing;

    public OrderService(UserRepository userRepository,
                        TicketService ticketService,
                        OrderRepository orderRepository,
                        PaymentRepository paymentRepository,
                        @Qualifier("regularPricing") PricingStrategy regularPricing,
                        @Qualifier("groupPricing") PricingStrategy groupPricing) {
        this.userRepository = userRepository;
        this.ticketService = ticketService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.regularPricing = regularPricing;
        this.groupPricing = groupPricing;
    }

    public OrderDtos.OrderResponse buy(String userEmail, OrderDtos.BuyTicketRequest request) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new NotFoundException("User not found"));
        Ticket ticket = ticketService.getEntityById(request.ticketId());

        if (ticket.getQuantity() < request.amount()) {
            throw new BadRequestException("Not enough tickets");
        }

        PricingStrategy strategy = request.amount() >= 5 ? groupPricing : regularPricing;
        var total = strategy.calculate(ticket.getPrice(), request.amount());

        ticket.setQuantity(ticket.getQuantity() - request.amount());
        ticketService.save(ticket);

        Order order = new Order();
        order.setUser(user);
        order.setTicket(ticket);
        order.setAmount(request.amount());
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PAID);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setAmount(total);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return toResponse(savedOrder);
    }

    public List<OrderDtos.OrderResponse> myOrders(String email) {
        Long userId = userRepository.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    public List<OrderDtos.OrderResponse> allOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    private OrderDtos.OrderResponse toResponse(Order order) {
        return new OrderDtos.OrderResponse(order.getId(), order.getTicket().getId(), order.getTicket().getEvent().getTitle(),
                order.getAmount(), order.getTotalPrice(), order.getStatus().name(), order.getCreatedAt());
    }
}
