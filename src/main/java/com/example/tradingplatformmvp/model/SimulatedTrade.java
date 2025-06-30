package com.example.tradingplatformmvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class SimulatedTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String symbol;
    private LocalDateTime timestamp;
    private String tradeType; // BUY or SELL
    private double price;
    private double quantity;
    private String strategyName;
    private double cashAfterTrade;
    private double portfolioValueAfterTrade;

}
