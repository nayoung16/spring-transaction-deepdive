package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.event.EventRequestDto;
import com.application.springtransaction.dto.event.EventResponseDto;
import com.application.springtransaction.mapper.EventMapper;
import com.application.springtransaction.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventResponseDto createEvent(EventRequestDto eventRequestDto) {
        Event savedEvent = eventRepository.save(eventMapper.toEntity(eventRequestDto));
        return eventMapper.toDto(savedEvent);
    }

    public List<EventResponseDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(eventMapper::toDto).toList();
    }


    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        return eventMapper.toDto(event);

    }
}
