package com.example.eventtourism.repository;

import com.example.eventtourism.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByEventDateGreaterThanEqualAndBasePriceLessThanEqualAndLocation_CityContainingIgnoreCase(
            LocalDate eventDate, BigDecimal basePrice, String city, Pageable pageable);
}
