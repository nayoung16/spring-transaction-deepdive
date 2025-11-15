package com.application.springtransaction.dto.purchase;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PurchaseResponseDto {
    private Long id;
    private String userName;
    private int quantity;
    private LocalDateTime purchaseDate;
}
