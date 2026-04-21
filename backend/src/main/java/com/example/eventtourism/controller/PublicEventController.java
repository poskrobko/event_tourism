package com.example.eventtourism.controller;

import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.facade.EventFacade;
import com.example.eventtourism.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/public/events")
public class PublicEventController {

    private final EventService eventService;
    private final EventFacade eventFacade;

    public PublicEventController(EventService eventService, EventFacade eventFacade) {
        this.eventService = eventService;
        this.eventFacade = eventFacade;
    }

    @GetMapping
    public Page<EventDtos.EventResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return eventService.findAll(date, city, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public EventDtos.EventDetailsResponse details(@PathVariable Long id) {
        return eventFacade.getEventDetails(id);
    }
}
