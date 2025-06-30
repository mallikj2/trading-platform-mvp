package com.example.tradingplatformmvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class TradingStrategyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String strategyName;
    private String symbol;
    private String parameters; // JSON string to store strategy-specific parameters
    private boolean enabled;

}
