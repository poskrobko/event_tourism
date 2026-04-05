package com.example.eventtourism.service;

import com.example.eventtourism.dto.EventDtos;
import com.example.eventtourism.entity.EventProgram;
import com.example.eventtourism.exception.NotFoundException;
import com.example.eventtourism.repository.EventProgramRepository;
import com.example.eventtourism.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramService {

    private final EventProgramRepository programRepository;
    private final EventRepository eventRepository;

    public ProgramService(EventProgramRepository programRepository, EventRepository eventRepository) {
        this.programRepository = programRepository;
        this.eventRepository = eventRepository;
    }

    public EventDtos.EventProgramResponse addProgram(Long eventId, EventDtos.EventProgramRequest request) {
        EventProgram program = new EventProgram();
        program.setEvent(eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found")));
        program.setStartTime(request.startTime());
        program.setActivity(request.activity());
        return toResponse(programRepository.save(program));
    }

    public List<EventDtos.EventProgramResponse> getProgram(Long eventId) {
        return programRepository.findByEvent_IdOrderByStartTimeAsc(eventId)
                .stream().map(this::toResponse).toList();
    }

    private EventDtos.EventProgramResponse toResponse(EventProgram p) {
        return new EventDtos.EventProgramResponse(p.getId(), p.getStartTime(), p.getActivity());
    }
}
