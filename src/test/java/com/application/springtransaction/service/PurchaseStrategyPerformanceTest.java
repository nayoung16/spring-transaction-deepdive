package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.repository.EventRepository;
import com.application.springtransaction.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@SpringBootTest
public class PurchaseStrategyPerformanceTest {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired PurchaseService purchaseService;

    private enum Strategy { NAIVE, ATOMIC, PESSIMISTIC }

    @BeforeEach
    void clean() {
        purchaseRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void naive_atomic_pessimistic_성능_비교() throws Exception {
        runAndPrint(Strategy.NAIVE);
        runAndPrint(Strategy.ATOMIC);
        runAndPrint(Strategy.PESSIMISTIC);
    }

    private void runAndPrint(Strategy strategy) throws Exception {
        // given
        Event event = Event.builder()
                .name("perf-" + strategy)
                .totalStock(50)
                .remainingStock(50)
                .build();
        Event savedEvent = eventRepository.save(event);

        int threadCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        long start = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    PurchaseRequestDto dto = new PurchaseRequestDto();
                    //dto.setUserName("user-" + idx);
                    //dto.setQuantity(1);

                    switch (strategy) {
                        case NAIVE:
                            purchaseService.savePurchaseNaive(dto, savedEvent.getId());
                            break;
                        case ATOMIC:
                            purchaseService.savePurchaseAtomic(dto, savedEvent.getId());
                            break;
                        case PESSIMISTIC:
                            purchaseService.savePurchasePessimistic(dto, savedEvent.getId());
                            break;
                    }
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        long end = System.nanoTime();
        long elapsed = (end - start) / 1_000_000;

        long purchases = purchaseRepository.countByEventId(savedEvent.getId());
        Event after = eventRepository.findById(savedEvent.getId()).orElseThrow();

        System.out.println("======================================");
        System.out.println("[Strategy]       = " + strategy);
        System.out.println("elapsed(ms)      = " + elapsed);
        System.out.println("success threads  = " + success.get());
        System.out.println("fail threads     = " + fail.get());
        System.out.println("purchases(sum)   = " + purchases);
        System.out.println("remainingStock   = " + after.getRemainingStock());
        System.out.println("sum(check)       = " + (purchases + after.getRemainingStock()));
        System.out.println("======================================");
    }
}
