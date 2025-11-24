package com.application.springtransaction.service;

import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseFacade {
    private final PurchaseService purchaseService;

    public void savePurchaseOptimisticWithRetry(PurchaseRequestDto dto, Long eventId) {
        int maxRetry = 5;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                purchaseService.savePurchaseOptimistic(dto, eventId);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == maxRetry) {
                    throw e;
                }
            }
        }
    }
}
