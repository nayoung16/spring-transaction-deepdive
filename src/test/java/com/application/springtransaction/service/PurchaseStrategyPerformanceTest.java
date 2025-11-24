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
import java.util.function.IntConsumer;

@SpringBootTest
public class PurchaseStrategyPerformanceTest {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired PurchaseService purchaseService;

    @Autowired PurchaseFacade purchaseFacade;

    private static final int THREAD_COUNT = 200;
    private static final int PERF_STOCK = 50;
    private static final int POLICY_STOCK = 200;
    private static final int USER_POOL_SIZE = 3;
    private static final int MAX_PER_USER = 5;

    private enum Strategy { NAIVE, ATOMIC, PESSIMISTIC }
    private enum PolicyStrategy { ATOMIC, PESSIMISTIC, OPTIMISTIC}

    private static class ConcurrencyResult {
        final long elapsedMs;
        final int success;
        final int fail;

        ConcurrencyResult(long elapsedMs, int success, int fail) {
            this.elapsedMs = elapsedMs;
            this.success = success;
            this.fail = fail;
        }
    }

    private ConcurrencyResult runConcurrent(int threadCount, IntConsumer taskPerIndex) throws Exception {
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
                    taskPerIndex.accept(idx);
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

        return new ConcurrencyResult(elapsedMs, success.get(), fail.get());
    }

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
        Event savedEvent = createEvent("perf-" + strategy, PERF_STOCK);

        int threadCount = THREAD_COUNT;

        ConcurrencyResult result = runConcurrent(threadCount, idx -> {
            PurchaseRequestDto dto = new PurchaseRequestDto();
            //dto.setUserName("user-" + idx);
            //dto.setQuantity(1);

            switch (strategy) {
                case NAIVE -> purchaseService.savePurchaseNaive(dto, savedEvent.getId());
                case ATOMIC -> purchaseService.savePurchaseAtomic(dto, savedEvent.getId());
                case PESSIMISTIC -> purchaseService.savePurchasePessimistic(dto, savedEvent.getId());
            }
        });

        long purchases = purchaseRepository.countByEventId(savedEvent.getId());
        Event after = eventRepository.findById(savedEvent.getId()).orElseThrow();

        System.out.println("======================================");
        System.out.println("[Strategy]       = " + strategy);
        System.out.println("elapsed(ms)      = " + result.elapsedMs);
        System.out.println("success threads  = " + result.success);
        System.out.println("fail threads     = " + result.fail);
        System.out.println("purchases(sum)   = " + purchases);
        System.out.println("remainingStock   = " + after.getRemainingStock());
        System.out.println("sum(check)       = " + (purchases + after.getRemainingStock()));
        System.out.println("======================================");
    }

    @Test
    void 정책_동시성_전략별_비교() throws Exception {
        for (PolicyStrategy strategy : PolicyStrategy.values()) {
            Event saved = createEvent("policy-" + strategy, POLICY_STOCK);

            runStrategy(
                    strategy.name(),
                    THREAD_COUNT,
                    USER_POOL_SIZE,
                    saved.getId(),
                    strategy
            );
        }
    }


    private void runStrategy(
            String name,
            int threadCount,
            int userPoolSize,
            Long eventId,
            PolicyStrategy strategy
    ) throws Exception {

        ConcurrencyResult result = runConcurrent(threadCount, idx -> {
            PurchaseRequestDto dto = new PurchaseRequestDto();
            //dto.setQuantity(1);
            //dto.setUserName("user-" + (idx % userPoolSize));

            switch (strategy) {
                case ATOMIC -> purchaseService.savePurchaseAtomic(dto, eventId);
                case PESSIMISTIC -> purchaseService.savePurchasePessimistic(dto, eventId);
                case OPTIMISTIC -> purchaseFacade.savePurchaseOptimisticWithRetry(dto, eventId);
            }
        });

        Event after = eventRepository.findById(eventId).orElseThrow();

        Map<String, Integer> perUser = new HashMap<>();
        purchaseRepository.findByEventId(eventId).forEach(p -> {
            perUser.merge(p.getUserName(), p.getQuantity(), Integer::sum);
        });

        boolean violated = perUser.values().stream()
                .anyMatch(q -> q > MAX_PER_USER);

        System.out.println("======================================");
        System.out.println("[Strategy]       = " + name);
        System.out.println("elapsed(ms)      = " + result.elapsedMs);
        System.out.println("success threads  = " + result.success);
        System.out.println("fail threads     = " + result.fail);
        System.out.println("remainingStock   = " + after.getRemainingStock());
        System.out.println("policy violated? = " + violated);
        System.out.println("perUser          = " + perUser);
        System.out.println("======================================\n");
    }

    private Event createEvent(String name, int quantity) {
        Event event = Event.builder()
                .name(name)
                .totalStock(quantity)
                .remainingStock(quantity)
                .build();
        return eventRepository.save(event);
    }
}
