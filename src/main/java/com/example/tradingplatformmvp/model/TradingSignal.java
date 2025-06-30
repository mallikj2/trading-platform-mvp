package com.example.tradingplatformmvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TradingSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String symbol;
    private LocalDateTime timestamp;
    private SignalType signalType;
    private String strategyName;
    private String description;

    public enum SignalType {
        BUY,
        SELL
    }
}