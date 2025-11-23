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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final EventRepository eventRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found. id=" + eventId));
    }

    @Transactional
    public void savePurchaseNaive(PurchaseRequestDto dto, Long eventId) {
        Event event = findEventById(eventId);
        validateQuantity(dto.getQuantity());

        event.deductStock(dto.getQuantity());
        eventRepository.save(event);

        savePurchaseRecord(dto, event);
    }

    @Transactional
    public void savePurchaseAtomic(PurchaseRequestDto dto, Long eventId) {
        Event event = findEventById(eventId);
        validateQuantity(dto.getQuantity());

        // UPDATE event SET remaining_stock = remaining_stock - ? WHERE id = ? AND remaining_stock >= ?
        int updatedRows = eventRepository.decrementStockIfEnough(eventId, dto.getQuantity());
        if (updatedRows == 0) {
            throw new IllegalStateException("Not enough stock");
        }

        savePurchaseRecord(dto, event);
    }


    @Transactional
    public void savePurchasePessimistic(PurchaseRequestDto dto, Long eventId) {
        Event event = findEventById(eventId);
        validateQuantity(dto.getQuantity());

        int updatedRows = eventRepository.decrementStockIfEnough(eventId, dto.getQuantity());
        if (updatedRows == 0) {
            throw new IllegalStateException("Not enough stock");
        }

        savePurchaseRecord(dto, event);
    }

    private void validateQuantity(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private void savePurchaseRecord(PurchaseRequestDto dto, Event event) {
        Purchase purchase = purchaseMapper.toEntity(dto, event);
        purchaseRepository.save(purchase);
    }

    public List<PurchaseResponseDto> findPurchaseByUserName(String userName) {
        return purchaseRepository.findPurchaseByUserName(userName)
                .stream()
                .map(purchaseMapper::toDto)
                .toList();
    }

}
