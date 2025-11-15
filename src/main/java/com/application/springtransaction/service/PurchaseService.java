package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.domain.Purchase;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.dto.purchase.PurchaseResponseDto;
import com.application.springtransaction.mapper.PurchaseMapper;
import com.application.springtransaction.repository.EventRepository;
import com.application.springtransaction.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final EventRepository eventRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;

    @Transactional
    public void savePurchase(PurchaseRequestDto purchaseRequestDto, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found. id = " + eventId));

        int qty = purchaseRequestDto.getQuantity();
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int updatedRows = eventRepository.decrementStockIfEnough(eventId, qty);
        if (updatedRows == 0) {
            throw new IllegalStateException("Not enough stock");
        }

        Purchase purchase = purchaseMapper.toEntity(purchaseRequestDto, event);
        purchaseRepository.save(purchase);
    }

    public List<PurchaseResponseDto> findPurchaseByUserName(String userName) {
        return purchaseRepository.findPurchaseByUserName(userName)
                .stream()
                .map(purchaseMapper::toDto)
                .toList();
    }

}
