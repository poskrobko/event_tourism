package com.example.eventtourism.controller;

import com.example.eventtourism.dto.CommonDtos;
import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.dto.OrderDtos;
import com.example.eventtourism.entity.Location;
import com.example.eventtourism.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EventService eventService;
    private final ProgramService programService;
    private final TicketService ticketService;
    private final OrderService orderService;
    private final LocationService locationService;

    public AdminController(EventService eventService, ProgramService programService, TicketService ticketService,
                           OrderService orderService, LocationService locationService) {
        this.eventService = eventService;
        this.programService = programService;
        this.ticketService = ticketService;
        this.orderService = orderService;
        this.locationService = locationService;
    }

    @PostMapping("/locations")
    public Location createLocation(@RequestBody Map<String, String> body) {
        return locationService.create(body.get("city"), body.get("address"));
    }

    @PostMapping("/events")
    public EventDtos.EventResponse createEvent(@Valid @RequestBody EventDtos.EventCreateRequest request) {
        return eventService.create(request);
    }

    @DeleteMapping("/events/{id}")
    public CommonDtos.ApiMessage deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
        return new CommonDtos.ApiMessage("Event deleted");
    }

    @PostMapping("/events/{eventId}/program")
    public EventDtos.EventProgramResponse addProgram(@PathVariable Long eventId,
                                                     @Valid @RequestBody EventDtos.EventProgramRequest request) {
        return programService.addProgram(eventId, request);
    }

    @PostMapping("/events/{eventId}/tickets")
    public EventDtos.TicketResponse createTicket(@PathVariable Long eventId,
                                                 @Valid @RequestBody EventDtos.TicketRequest request) {
        return ticketService.createTicket(eventId, request);
    }

    @GetMapping("/orders")
    public List<OrderDtos.OrderResponse> allOrders() {
        return orderService.allOrders();
    }
}
