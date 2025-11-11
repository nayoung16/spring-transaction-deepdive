package com.application.springtransaction.mapper;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.event.EventRequestDto;
import com.application.springtransaction.dto.event.EventResponseDto;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequestDto dto) {
        return Event.builder()
                .name(dto.getName())
                .totalStock(dto.getTotalStock())
                .remainingStock(dto.getTotalStock())
                .build();
    }

    public EventResponseDto toDto(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .totalStock(event.getTotalStock())
                .remainingStock(event.getRemainingStock())
                .createdAt(event.getCreatedAt())
                .build();
    }
}