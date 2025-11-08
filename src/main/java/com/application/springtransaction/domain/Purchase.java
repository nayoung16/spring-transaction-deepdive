package com.application.springtransaction.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private String userName;
    private int quantity;

    private LocalDateTime createdAt;
}
