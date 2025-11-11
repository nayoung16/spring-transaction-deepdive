package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.EventRequestDto;
import com.application.springtransaction.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Event createEvent(EventRequestDto eventRequestDto) {
        Event event = Event.builder()
                .name(eventRequestDto.getName())
                .totalStock(eventRequestDto.getTotalStock()).build();
        return eventRepository.save(event);
    }
}
