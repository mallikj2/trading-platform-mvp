package com.example.tradingplatformmvp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TradeExecutionDto {
    private String symbol;
    private LocalDateTime timestamp;
    private String tradeType; // BUY or SELL
    private double price;
    private double quantity;
    private String strategyName;
    private double currentCash;
    private double currentPortfolioValue; // Optional, for more detailed tracking
}
