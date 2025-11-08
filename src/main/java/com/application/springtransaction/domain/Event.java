package com.application.springtransaction.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int totalStock;
    private int remainingStock;
    private LocalDateTime createdAt;
}
