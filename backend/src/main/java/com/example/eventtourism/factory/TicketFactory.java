package com.example.eventtourism.factory;

import com.example.eventtourism.entity.Event;
import com.example.eventtourism.entity.Ticket;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TicketFactory {
    public Ticket create(Event event, String category, BigDecimal price, Integer quantity) {
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setCategory(category);
        ticket.setPrice(price);
        ticket.setQuantity(quantity);
        return ticket;
    }
}
