package com.application.springtransaction.mapper;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.domain.Purchase;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.dto.purchase.PurchaseResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {

    public Purchase toEntity(PurchaseRequestDto dto, Event event) {
        return Purchase.builder()
                .event(event)
                .userName(dto.getUserName())
                .quantity(dto.getQuantity())
                .build();
    }

    public PurchaseResponseDto toDto(Purchase purchase) {
        return PurchaseResponseDto.builder()
                .id(purchase.getId())
                .userName(purchase.getUserName())
                .quantity(purchase.getQuantity())
                .purchaseDate(purchase.getCreatedAt())
                .build();
    }
}
