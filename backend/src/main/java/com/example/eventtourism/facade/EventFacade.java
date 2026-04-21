package com.example.eventtourism.facade;

import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.service.EventService;
import com.example.eventtourism.service.ProgramService;
import com.example.eventtourism.service.TicketService;
import org.springframework.stereotype.Component;

@Component
public class EventFacade {

    private final EventService eventService;
    private final ProgramService programService;
    private final TicketService ticketService;

    public EventFacade(EventService eventService, ProgramService programService, TicketService ticketService) {
        this.eventService = eventService;
        this.programService = programService;
        this.ticketService = ticketService;
    }

    public EventDtos.EventDetailsResponse getEventDetails(Long eventId) {
        return new EventDtos.EventDetailsResponse(
                eventService.getEventById(eventId),
                programService.getProgram(eventId),
                ticketService.getTicketsByEvent(eventId),
                eventService.getCalendarLink(eventId)
        );
    }
}
