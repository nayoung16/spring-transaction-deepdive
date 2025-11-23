package com.application.springtransaction.dto.purchase;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PurchaseRequestDto {
    private String userName;
    private int quantity;
}
