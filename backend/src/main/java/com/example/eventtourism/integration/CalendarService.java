package com.example.eventtourism.integration;

import com.example.eventtourism.entity.Event;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {
    public String createCalendarLink(Event event) {
        return "https://calendar.google.com/calendar/render?action=TEMPLATE&text=" + event.getTitle().replace(" ", "+");
    }
}
