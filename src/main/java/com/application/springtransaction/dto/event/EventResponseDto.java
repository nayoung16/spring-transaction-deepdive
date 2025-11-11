package com.application.springtransaction.dto.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class EventResponseDto {
    private Long id;
    private String name;
    private int totalStock;
    private int remainingStock;
    private LocalDateTime createdAt;
}
