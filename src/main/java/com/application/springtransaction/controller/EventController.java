package com.application.springtransaction.controller;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.event.EventRequestDto;
import com.application.springtransaction.dto.event.EventResponseDto;
import com.application.springtransaction.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventRequestDto eventRequestDto) {
        EventResponseDto event = eventService.createEvent(eventRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        List<EventResponseDto> allEvents = eventService.getAllEvents();
        return ResponseEntity.status(HttpStatus.OK).body(allEvents);
    }
}
