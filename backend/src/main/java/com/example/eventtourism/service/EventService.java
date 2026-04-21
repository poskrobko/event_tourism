package com.example.eventtourism.service;

import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.entity.Event;
import com.example.eventtourism.exception.NotFoundException;
import com.example.eventtourism.integration.CalendarService;
import com.example.eventtourism.repository.EventRepository;
import com.example.eventtourism.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CalendarService calendarService;

    public EventService(EventRepository eventRepository, LocationRepository locationRepository, CalendarService calendarService) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.calendarService = calendarService;
    }

    public EventDtos.EventResponse create(EventDtos.EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        event.setBasePrice(request.basePrice());
        event.setLocation(locationRepository.findById(request.locationId())
                .orElseThrow(() -> new NotFoundException("Location not found")));
        return toResponse(eventRepository.save(event));
    }

    public Page<EventDtos.EventResponse> findAll(LocalDate date, String city, BigDecimal maxPrice, Pageable pageable) {
        LocalDate filterDate = date == null ? LocalDate.now() : date;
        String filterCity = city == null ? "" : city;
        BigDecimal filterPrice = maxPrice == null ? BigDecimal.valueOf(1_000_000) : maxPrice;

        return eventRepository.findByEventDateGreaterThanEqualAndBasePriceLessThanEqualAndLocation_CityContainingIgnoreCase(
                filterDate, filterPrice, filterCity, pageable
        ).map(this::toResponse);
    }

    public EventDtos.EventResponse getEventById(Long id) {
        return toResponse(eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found")));
    }

    public String getCalendarLink(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        return calendarService.createCalendarLink(event);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    private EventDtos.EventResponse toResponse(Event e) {
        return new EventDtos.EventResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getEventDate(), e.getBasePrice(),
                e.getLocation().getCity(), e.getLocation().getAddress(), e.getLocation().getMapUrl()
        );
    }
}
