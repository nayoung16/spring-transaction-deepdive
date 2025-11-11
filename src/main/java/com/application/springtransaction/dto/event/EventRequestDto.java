package com.application.springtransaction.dto.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // for test
public class EventRequestDto {
    private String name;
    private int totalStock;
}
