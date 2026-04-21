package com.example.eventtourism.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "event_programs")
public class EventProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private String activity;

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
}
