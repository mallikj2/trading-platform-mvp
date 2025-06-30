package com.example.tradingplatformmvp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IndicatorDto {
    private String symbol;
    private LocalDateTime timestamp;
    private double sma;
    private double rsi;
    private double macd;
    private double macdSignal;
    private double macdHist;
}
