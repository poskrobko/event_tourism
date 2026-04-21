package com.example.eventtourism.service;

import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.entity.Event;
import com.example.eventtourism.entity.Ticket;
import com.example.eventtourism.exception.NotFoundException;
import com.example.eventtourism.factory.TicketFactory;
import com.example.eventtourism.repository.EventRepository;
import com.example.eventtourism.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final TicketFactory ticketFactory;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository, TicketFactory ticketFactory) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.ticketFactory = ticketFactory;
    }

    public EventDtos.TicketResponse createTicket(Long eventId, EventDtos.TicketRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        Ticket ticket = ticketFactory.create(event, request.category(), request.price(), request.quantity());
        return toResponse(ticketRepository.save(ticket));
    }

    public List<EventDtos.TicketResponse> getTicketsByEvent(Long eventId) {
        return ticketRepository.findByEvent_Id(eventId).stream().map(this::toResponse).toList();
    }

    public Ticket getEntityById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    public void save(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    private EventDtos.TicketResponse toResponse(Ticket t) {
        return new EventDtos.TicketResponse(t.getId(), t.getEvent().getId(), t.getEvent().getTitle(), t.getCategory(), t.getPrice(), t.getQuantity());
    }
}
