package com.example.tradingplatformmvp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MlPredictionDto {
    private String symbol;
    private LocalDateTime timestamp;
    private String prediction;
    private double confidence;
}
