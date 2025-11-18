package com.application.springtransaction.repository;

import com.application.springtransaction.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findPurchaseByUserName(String userName);

    @Query("select sum(p.quantity) from Purchase p where p.event.id = :eventId")
    long countByEventId(@Param("eventId") long eventId);
}
