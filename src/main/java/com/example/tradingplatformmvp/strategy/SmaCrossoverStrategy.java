package com.example.tradingplatformmvp.strategy;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.service.TechnicalAnalysisService;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmaCrossoverStrategy implements TradingStrategy {

    private final TechnicalAnalysisService technicalAnalysisService;

    // Store last SMA values for each symbol to detect crossovers
    private final ConcurrentHashMap<String, Double> lastShortSma = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> lastLongSma = new ConcurrentHashMap<>();

    private final int shortSmaPeriod = 5; // Example short period
    private final int longSmaPeriod = 20; // Example long period

    public SmaCrossoverStrategy(TechnicalAnalysisService technicalAnalysisService) {
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @Override
    public List<TradingSignal> generateSignals(List<StockData> historicalData, StockData currentData) {
        List<TradingSignal> signals = new ArrayList<>();

        // Add the current incoming data point to the historical data for analysis
        List<StockData> dataForAnalysis = new ArrayList<>(historicalData);
        dataForAnalysis.add(currentData);

        // Ensure enough data for SMA calculation
        if (dataForAnalysis.size() < longSmaPeriod) {
            // System.out.println("Not enough data for SMA crossover strategy for " + currentData.getSymbol());
            return signals;
        }

        BarSeries series = technicalAnalysisService.buildBarSeries(dataForAnalysis);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaPeriod);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaPeriod);

        double currentShortSma = shortSma.getValue(series.getEndIndex()).doubleValue();
        double currentLongSma = longSma.getValue(series.getEndIndex()).doubleValue();

        Double prevShortSma = lastShortSma.get(currentData.getSymbol());
        Double prevLongSma = lastLongSma.get(currentData.getSymbol());

        // Update last SMA values
        lastShortSma.put(currentData.getSymbol(), currentShortSma);
        lastLongSma.put(currentData.getSymbol(), currentLongSma);

        // Generate signals based on SMA crossover
        if (prevShortSma != null && prevLongSma != null) {
            // Buy signal: Short SMA crosses above Long SMA
            if (currentShortSma > currentLongSma && prevShortSma <= prevLongSma) {
                TradingSignal buySignal = new TradingSignal();
                buySignal.setSymbol(currentData.getSymbol());
                buySignal.setTimestamp(currentData.getTimestamp());
                buySignal.setSignalType(TradingSignal.SignalType.BUY);
                buySignal.setStrategyName(getName());
                buySignal.setDescription(String.format("BUY: Short SMA (%.2f) crossed above Long SMA (%.2f)", currentShortSma, currentLongSma));
                signals.add(buySignal);
            }
            // Sell signal: Short SMA crosses below Long SMA
            else if (currentShortSma < currentLongSma && prevShortSma >= prevLongSma) {
                TradingSignal sellSignal = new TradingSignal();
                sellSignal.setSymbol(currentData.getSymbol());
                sellSignal.setTimestamp(currentData.getTimestamp());
                sellSignal.setSignalType(TradingSignal.SignalType.SELL);
                sellSignal.setStrategyName(getName());
                sellSignal.setDescription(String.format("SELL: Short SMA (%.2f) crossed below Long SMA (%.2f)", currentShortSma, currentLongSma));
                signals.add(sellSignal);
            }
        }
        return signals;
    }

    @Override
    public String getName() {
        return "SMA_CROSSOVER_STRATEGY";
    }
}
