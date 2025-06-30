package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.SimulatedTrade;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.model.TradingSignal.SignalType;
import com.example.tradingplatformmvp.repository.SimulatedTradeRepository;
import com.example.tradingplatformmvp.repository.StockDataRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderManagementService {

    // Simulated portfolio: symbol -> quantity
    private final ConcurrentHashMap<String, Double> portfolio = new ConcurrentHashMap<>();
    private double cash = 10000.0; // Initial cash

    private final StockDataRepository stockDataRepository;
    private final SimulatedTradeRepository simulatedTradeRepository;

    public OrderManagementService(StockDataRepository stockDataRepository, SimulatedTradeRepository simulatedTradeRepository) {
        this.stockDataRepository = stockDataRepository;
        this.simulatedTradeRepository = simulatedTradeRepository;
    }

    @KafkaListener(topics = "trading-signals-topic", groupId = "trading-platform-group")
    public void consumeTradingSignal(TradingSignal signal) {
        System.out.println("OrderManagementService received signal: " + signal.getSymbol() + " - " + signal.getSignalType());

        // Fetch the latest stock data to get the current price
        List<StockData> latestData = stockDataRepository.findBySymbolOrderByTimestampAsc(signal.getSymbol());
        if (latestData.isEmpty()) {
            System.out.println("No recent stock data for " + signal.getSymbol() + ", cannot execute trade.");
            return;
        }
        double currentPrice = latestData.get(latestData.size() - 1).getClose();

        // For simplicity, we'll assume a fixed trade amount or quantity
        double tradeAmount = 100.0; // Trade $100 worth of stock

        if (signal.getSignalType() == SignalType.BUY) {
            if (cash >= tradeAmount) {
                double quantity = tradeAmount / currentPrice;
                portfolio.merge(signal.getSymbol(), quantity, Double::sum);
                cash -= tradeAmount;
                System.out.println(String.format("SIMULATED BUY: %s %.2f units at %.2f. New Cash: %.2f, Portfolio: %s",
                        signal.getSymbol(), quantity, currentPrice, cash, portfolio));
                saveSimulatedTrade(signal, "BUY", currentPrice, quantity);
            } else {
                System.out.println("SIMULATED BUY FAILED: Insufficient cash for " + signal.getSymbol());
            }
        } else if (signal.getSignalType() == SignalType.SELL) {
            double heldQuantity = portfolio.getOrDefault(signal.getSymbol(), 0.0);
            if (heldQuantity * currentPrice >= tradeAmount) { // Check if we hold enough to sell the desired amount
                double quantityToSell = tradeAmount / currentPrice;
                portfolio.merge(signal.getSymbol(), -quantityToSell, Double::sum);
                cash += tradeAmount;
                System.out.println(String.format("SIMULATED SELL: %s %.2f units at %.2f. New Cash: %.2f, Portfolio: %s",
                        signal.getSymbol(), quantityToSell, currentPrice, cash, portfolio));
                saveSimulatedTrade(signal, "SELL", currentPrice, quantityToSell);
            } else {
                System.out.println("SIMULATED SELL FAILED: Insufficient holdings for " + signal.getSymbol());
            }
        }
    }

    private void saveSimulatedTrade(TradingSignal signal, String tradeType, double price, double quantity) {
        SimulatedTrade simulatedTrade = new SimulatedTrade();
        simulatedTrade.setSymbol(signal.getSymbol());
        simulatedTrade.setTimestamp(LocalDateTime.now());
        simulatedTrade.setTradeType(tradeType);
        simulatedTrade.setPrice(price);
        simulatedTrade.setQuantity(quantity);
        simulatedTrade.setStrategyName(signal.getStrategyName());
        simulatedTrade.setCashAfterTrade(cash);
        // For portfolioValueAfterTrade, you would sum up (quantity * currentPrice) for all holdings
        simulatedTrade.setPortfolioValueAfterTrade(calculatePortfolioValue());
        simulatedTradeRepository.save(simulatedTrade);
    }

    private double calculatePortfolioValue() {
        // This is a simplified calculation. In a real app, you'd fetch current prices for all held symbols.
        return cash + portfolio.entrySet().stream()
                .mapToDouble(entry -> {
                    List<StockData> latestData = stockDataRepository.findBySymbolOrderByTimestampAsc(entry.getKey());
                    if (!latestData.isEmpty()) {
                        return entry.getValue() * latestData.get(latestData.size() - 1).getClose();
                    }
                    return 0.0;
                })
                .sum();
    }
}
