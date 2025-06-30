package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.model.TradingSignal.SignalType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderManagementService {

    // Simulated portfolio: symbol -> quantity
    private final ConcurrentHashMap<String, Double> portfolio = new ConcurrentHashMap<>();
    private double cash = 10000.0; // Initial cash

    @KafkaListener(topics = "trading-signals-topic", groupId = "trading-platform-group")
    public void consumeTradingSignal(TradingSignal signal) {
        System.out.println("OrderManagementService received signal: " + signal.getSymbol() + " - " + signal.getSignalType());

        // For simplicity, we'll assume a fixed trade amount or quantity
        double tradeAmount = 100.0; // Trade $100 worth of stock

        // In a real scenario, you'd fetch current price, check available capital/holdings,
        // and interact with a brokerage API.

        // Simulate current price (for demonstration, use a placeholder or last known price)
        double currentPrice = 1.0; // Placeholder: In real app, get actual current price

        if (signal.getSignalType() == SignalType.BUY) {
            if (cash >= tradeAmount) {
                double quantity = tradeAmount / currentPrice;
                portfolio.merge(signal.getSymbol(), quantity, Double::sum);
                cash -= tradeAmount;
                System.out.println(String.format("SIMULATED BUY: %s %.2f units. New Cash: %.2f, Portfolio: %s",
                        signal.getSymbol(), quantity, cash, portfolio));
                // Publish a TradeExecution event to Kafka (future phase)
            } else {
                System.out.println("SIMULATED BUY FAILED: Insufficient cash for " + signal.getSymbol());
            }
        } else if (signal.getSignalType() == SignalType.SELL) {
            double heldQuantity = portfolio.getOrDefault(signal.getSymbol(), 0.0);
            if (heldQuantity * currentPrice >= tradeAmount) { // Check if we hold enough to sell the desired amount
                double quantityToSell = tradeAmount / currentPrice;
                portfolio.merge(signal.getSymbol(), -quantityToSell, Double::sum);
                cash += tradeAmount;
                System.out.println(String.format("SIMULATED SELL: %s %.2f units. New Cash: %.2f, Portfolio: %s",
                        signal.getSymbol(), quantityToSell, cash, portfolio));
                // Publish a TradeExecution event to Kafka (future phase)
            } else {
                System.out.println("SIMULATED SELL FAILED: Insufficient holdings for " + signal.getSymbol());
            }
        }
    }
}
