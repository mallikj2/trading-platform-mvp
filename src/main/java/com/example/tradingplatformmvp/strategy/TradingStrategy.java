package com.example.tradingplatformmvp.strategy;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;

import java.util.List;

public interface TradingStrategy {
    List<TradingSignal> generateSignals(List<StockData> historicalData, StockData currentData);
    String getName();
}
