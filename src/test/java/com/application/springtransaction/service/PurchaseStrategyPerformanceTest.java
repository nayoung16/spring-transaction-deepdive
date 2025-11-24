package com.application.springtransaction.service;

import com.application.springtransaction.domain.Event;
import com.application.springtransaction.dto.purchase.PurchaseRequestDto;
import com.application.springtransaction.repository.EventRepository;
import com.application.springtransaction.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
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

    @Autowired PurchaseFacade purchaseFacade;

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

    @Test
    void atomic_유저한도_동시성_비교() throws Exception {

        Event event = Event.builder()
                .name("policy-test")
                .totalStock(200)
                .remainingStock(200)
                .build();
        Event saved = eventRepository.save(event);

        int threadCount = 200;
        int userPoolSize = 3;

        runStrategy("ATOMIC", threadCount, userPoolSize, saved.getId(),
                (dto, id) -> purchaseService.savePurchaseAtomic(dto, id));
    }

    @Test
    void pessimistic_유저한도_동시성_비교() throws Exception {

        Event event = Event.builder()
                .name("policy-test")
                .totalStock(200)
                .remainingStock(200)
                .build();
        Event saved = eventRepository.save(event);

        int threadCount = 200;
        int userPoolSize = 3;

        runStrategy("PESSIMISTIC", threadCount, userPoolSize, saved.getId(),
                (dto, id) -> purchaseService.savePurchasePessimistic(dto, id));
    }

    @Test
    void optimistic_유저한도_동시성_비교() throws Exception {

        Event event = Event.builder()
                .name("policy-test")
                .totalStock(200)
                .remainingStock(200)
                .build();
        Event saved = eventRepository.save(event);

        int threadCount = 200;
        int userPoolSize = 3;

        runStrategy("OPTIMISTIC", threadCount, userPoolSize, saved.getId(),
                (dto, id) -> purchaseFacade.savePurchaseOptimisticWithRetry(dto, id));
    }

    private void runStrategy(
            String name,
            int threadCount,
            int userPoolSize,
            Long eventId,
            BiConsumer<PurchaseRequestDto, Long> action
    ) throws Exception {

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
                    dto.setQuantity(1);
                    dto.setUserName("user-" + (idx % userPoolSize));

                    action.accept(dto, eventId);
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
        long elapsedMs = (end - start) / 1_000_000;

        Event after = eventRepository.findById(eventId).orElseThrow();

        Map<String, Integer> perUser = new HashMap<>();
        purchaseRepository.findByEventId(eventId).forEach(p -> {
            perUser.merge(p.getUserName(), p.getQuantity(), Integer::sum);
        });

        boolean violated = perUser.values().stream()
                .anyMatch(q -> q > 5); // MAX_PER_USER=5 기준

        System.out.println("======================================");
        System.out.println("[Strategy]       = " + name);
        System.out.println("elapsed(ms)      = " + elapsedMs);
        System.out.println("success threads  = " + success.get());
        System.out.println("fail threads     = " + fail.get());
        System.out.println("remainingStock   = " + after.getRemainingStock());
        System.out.println("policy violated? = " + violated);
        System.out.println("perUser          = " + perUser);
        System.out.println("======================================\n");
    }
}
