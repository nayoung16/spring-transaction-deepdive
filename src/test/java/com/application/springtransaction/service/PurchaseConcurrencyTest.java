package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.repository.EventRepository;
import com.application.springtransaction.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PurchaseConcurrencyTest {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    PurchaseService purchaseService;

    @Test
    void 동시에_여러_구매_요청_보내보기() throws Exception {
        Event event = Event.builder()
                .name("동시성 테스트 이벤트")
                .totalStock(10)
                .remainingStock(10)
                .build();
        Event savedEvent = eventRepository.save(event);

        int threadCount = 10; // 10명이 동시에 산다고 가정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1); // 동시에 시작시키기 위한 래치
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 끝나는 거 기다리기 위한 래치

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    PurchaseRequestDto dto = new PurchaseRequestDto();
                    //dto.setUserName("user-" + idx);
                    //dto.setQuantity(1);

                    // 실제 구매 호출
                    purchaseService.savePurchase(dto, savedEvent.getId());
                } catch (Exception e) {
                    System.out.println("스레드 예외: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시에 출발
        startLatch.countDown();
        // 전부 끝날 때까지 대기
        doneLatch.await();

        // when: DB 상태 다시 확인
        long purchaseCount = purchaseRepository.countByEventId(savedEvent.getId());
        Event afterEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();

        System.out.println("생성된 Purchase 개수 = " + purchaseCount);
        System.out.println("남은 재고 = " + afterEvent.getRemainingStock());

        assertThat(purchaseCount).isEqualTo(10); // 10명이 다 성공했는지
        assertThat(afterEvent.getRemainingStock()).isEqualTo(0); // 현재 구현 기준
    }
}
