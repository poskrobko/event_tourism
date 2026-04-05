package com.example.eventtourism.repository;

import com.example.eventtourism.entity.EventProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventProgramRepository extends JpaRepository<EventProgram, Long> {
    List<EventProgram> findByEvent_IdOrderByStartTimeAsc(Long eventId);
}
