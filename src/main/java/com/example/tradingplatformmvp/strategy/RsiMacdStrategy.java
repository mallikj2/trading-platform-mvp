package com.example.tradingplatformmvp.strategy;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.service.TechnicalAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RsiMacdStrategy implements TradingStrategy {

    private final TechnicalAnalysisService technicalAnalysisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RsiMacdStrategy(TechnicalAnalysisService technicalAnalysisService) {
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @Override
    public List<TradingSignal> generateSignals(List<StockData> historicalData, StockData currentData, String parametersJson) {
        List<TradingSignal> signals = new ArrayList<>();

        // Default parameters
        int rsiPeriod = 14;
        double rsiOverbought = 70.0;
        double rsiOversold = 30.0;
        int macdFastPeriod = 12;
        int macdSlowPeriod = 26;
        int macdSignalPeriod = 9;

        try {
            Map<String, Object> params = objectMapper.readValue(parametersJson, Map.class);
            if (params.containsKey("rsiPeriod")) rsiPeriod = (int) params.get("rsiPeriod");
            if (params.containsKey("rsiOverbought")) rsiOverbought = (double) params.get("rsiOverbought");
            if (params.containsKey("rsiOversold")) rsiOversold = (double) params.get("rsiOversold");
            if (params.containsKey("macdFastPeriod")) macdFastPeriod = (int) params.get("macdFastPeriod");
            if (params.containsKey("macdSlowPeriod")) macdSlowPeriod = (int) params.get("macdSlowPeriod");
            if (params.containsKey("macdSignalPeriod")) macdSignalPeriod = (int) params.get("macdSignalPeriod");
        } catch (Exception e) {
            System.err.println("Error parsing RSI/MACD strategy parameters: " + e.getMessage());
        }

        // Add the current incoming data point to the historical data for analysis
        List<StockData> dataForAnalysis = new ArrayList<>(historicalData);
        dataForAnalysis.add(currentData);

        // Ensure enough data for calculations
        if (dataForAnalysis.size() < Math.max(Math.max(rsiPeriod, macdFastPeriod), macdSlowPeriod)) {
            return signals;
        }

        BarSeries series = technicalAnalysisService.buildBarSeries(dataForAnalysis);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // RSI
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiPeriod);
        double currentRsi = rsi.getValue(series.getEndIndex()).doubleValue();

        // MACD
        EMAIndicator emaFast = new EMAIndicator(closePrice, macdFastPeriod);
        EMAIndicator emaSlow = new EMAIndicator(closePrice, macdSlowPeriod);
        MACDIndicator macd = new MACDIndicator(closePrice, macdFastPeriod, macdSlowPeriod);
        EMAIndicator macdSignal = new EMAIndicator(macd, macdSignalPeriod);

        double currentMacd = macd.getValue(series.getEndIndex()).doubleValue();
        double currentMacdSignal = macdSignal.getValue(series.getEndIndex()).doubleValue();
        double previousMacd = macd.getValue(series.getEndIndex() - 1).doubleValue();
        double previousMacdSignal = macdSignal.getValue(series.getEndIndex() - 1).doubleValue();

        // Buy Condition: RSI oversold and MACD crosses above signal line
        if (currentRsi < rsiOversold && currentMacd > currentMacdSignal && previousMacd <= previousMacdSignal) {
            TradingSignal buySignal = new TradingSignal();
            buySignal.setSymbol(currentData.getSymbol());
            buySignal.setTimestamp(currentData.getTimestamp());
            buySignal.setSignalType(TradingSignal.SignalType.BUY);
            buySignal.setStrategyName(getName());
            buySignal.setDescription(String.format("BUY: RSI (%.2f) oversold & MACD (%.2f) crossed above Signal (%.2f)", currentRsi, currentMacd, currentMacdSignal));
            signals.add(buySignal);
        }
        // Sell Condition: RSI overbought and MACD crosses below signal line
        else if (currentRsi > rsiOverbought && currentMacd < currentMacdSignal && previousMacd >= previousMacdSignal) {
            TradingSignal sellSignal = new TradingSignal();
            sellSignal.setSymbol(currentData.getSymbol());
            sellSignal.setTimestamp(currentData.getTimestamp());
            sellSignal.setSignalType(TradingSignal.SignalType.SELL);
            sellSignal.setStrategyName(getName());
            sellSignal.setDescription(String.format("SELL: RSI (%.2f) overbought & MACD (%.2f) crossed below Signal (%.2f)", currentRsi, currentMacd, currentMacdSignal));
            signals.add(sellSignal);
        }

        return signals;
    }

    @Override
    public String getName() {
        return "RSI_MACD_STRATEGY";
    }
}
