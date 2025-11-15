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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final EventRepository eventRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;

    public Purchase savePurchase(PurchaseRequestDto purchaseRequestDto, Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        Purchase purchase = purchaseMapper.toEntity(purchaseRequestDto, event);
        return purchaseRepository.save(purchase);
    }

    public List<PurchaseResponseDto> findPurchaseByUserName(String userName) {
        List<Purchase> purchaseList = purchaseRepository.findPurchaseByUserName(userName);
        List<PurchaseResponseDto> dtoList = new ArrayList<>();
        for (Purchase purchase : purchaseList) {
            PurchaseResponseDto purchaseResponseDto = purchaseMapper.toDto(purchase);
            dtoList.add(purchaseResponseDto);
        }
        return dtoList;
    }

}
