package com.example.tradingplatformmvp.strategy;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.service.TechnicalAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmaCrossoverStrategy implements TradingStrategy {

    private final TechnicalAnalysisService technicalAnalysisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store last SMA values for each symbol to detect crossovers
    private final ConcurrentHashMap<String, Double> lastShortSma = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> lastLongSma = new ConcurrentHashMap<>();

    public SmaCrossoverStrategy(TechnicalAnalysisService technicalAnalysisService) {
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @Override
    public List<TradingSignal> generateSignals(List<StockData> historicalData, StockData currentData, String parametersJson) {
        List<TradingSignal> signals = new ArrayList<>();

        int shortSmaPeriod = 5; // Default
        int longSmaPeriod = 20; // Default

        try {
            Map<String, Integer> params = objectMapper.readValue(parametersJson, Map.class);
            if (params.containsKey("shortSma")) {
                shortSmaPeriod = params.get("shortSma");
            }
            if (params.containsKey("longSma")) {
                longSmaPeriod = params.get("longSma");
            }
        } catch (Exception e) {
            System.err.println("Error parsing SMA Crossover strategy parameters: " + e.getMessage());
        }

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
                buySignal.setDescription(String.format("BUY: Short SMA (%d) crossed above Long SMA (%d)", shortSmaPeriod, longSmaPeriod));
                signals.add(buySignal);
            }
            // Sell signal: Short SMA crosses below Long SMA
            else if (currentShortSma < currentLongSma && prevShortSma >= prevLongSma) {
                TradingSignal sellSignal = new TradingSignal();
                sellSignal.setSymbol(currentData.getSymbol());
                sellSignal.setTimestamp(currentData.getTimestamp());
                sellSignal.setSignalType(TradingSignal.SignalType.SELL);
                sellSignal.setStrategyName(getName());
                sellSignal.setDescription(String.format("SELL: Short SMA (%d) crossed below Long SMA (%d)", shortSmaPeriod, longSmaPeriod));
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
