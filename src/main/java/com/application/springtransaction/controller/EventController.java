package com.application.springtransaction.controller;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.event.EventRequestDto;
import com.application.springtransaction.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventRequestDto eventRequestDto) {
        Event event = eventService.createEvent(eventRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
}
