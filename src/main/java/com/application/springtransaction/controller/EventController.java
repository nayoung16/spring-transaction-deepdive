package com.application.springtransaction.controller;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.event.EventRequestDto;
import com.application.springtransaction.dto.event.EventResponseDto;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.service.EventService;
import com.application.springtransaction.service.PurchaseService;
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
    private final PurchaseService purchaseService;

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

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long eventId) {
        EventResponseDto eventById = eventService.getEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventById);
    }

    @PostMapping("/{eventId}/purchase")
    public ResponseEntity<?> purchaseEvent(@PathVariable Long eventId, @RequestBody PurchaseRequestDto purchaseRequestDto) {
        purchaseService.savePurchase(purchaseRequestDto, eventId);
        eventService.deductStock(eventId, purchaseRequestDto.getQuantity());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
