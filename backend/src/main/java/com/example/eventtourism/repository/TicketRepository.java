package com.example.eventtourism.repository;

import com.example.eventtourism.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEvent_Id(Long eventId);
}
