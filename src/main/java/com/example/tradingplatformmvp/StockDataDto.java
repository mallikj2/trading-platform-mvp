package com.example.tradingplatformmvp;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockDataDto {
    private String symbol;
    private LocalDateTime timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
}
