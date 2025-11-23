package com.application.springtransaction.repository;

import com.application.springtransaction.domain.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Modifying
    @Query(
            value = """
            UPDATE event
               SET remaining_stock = remaining_stock - :qty
             WHERE id = :eventId
               AND remaining_stock >= :qty
            """,
            nativeQuery = true
    )
    int decrementStockIfEnough(@Param("eventId") Long eventId,
                               @Param("qty") int quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
