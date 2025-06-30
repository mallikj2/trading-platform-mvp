package com.example.tradingplatformmvp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class BacktestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String strategyName;
    private String symbol;
    private LocalDate startDate;
    private LocalDate endDate;
    private double initialCapital;
    private double finalCapital;
    private double totalProfitLoss;
    private double percentageProfitLoss;
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private LocalDateTime backtestRunTime;
    private String description;

}