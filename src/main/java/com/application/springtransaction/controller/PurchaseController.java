package com.application.springtransaction.controller;

import com.application.springtransaction.dto.purchase.PurchaseResponseDto;
import com.application.springtransaction.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/{userName}")
    public ResponseEntity<List<PurchaseResponseDto>> getPurchaseByUserName(@PathVariable String userName) {
        List<PurchaseResponseDto> purchaseByUserName = purchaseService.findPurchaseByUserName(userName);
        return ResponseEntity.ok(purchaseByUserName);
    }
}
