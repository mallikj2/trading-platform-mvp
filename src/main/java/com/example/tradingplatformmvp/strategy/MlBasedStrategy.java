package com.example.tradingplatformmvp.strategy;

import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.dto.MlPredictionDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MlBasedStrategy implements TradingStrategy {

    // This strategy will directly consume MlPredictionDto, so historicalData and currentData are less relevant here
    // However, the interface requires them, so we'll keep them for now.
    @Override
    public List<TradingSignal> generateSignals(List<StockData> historicalData, StockData currentData, String parametersJson) {
        // This method will not be directly called by SignalGenerationService for ML predictions
        // ML predictions will be consumed by a separate KafkaListener in SignalGenerationService
        return new ArrayList<>();
    }

    // This method will be called by SignalGenerationService when an MlPredictionDto is received
    public List<TradingSignal> generateSignalsFromMlPrediction(MlPredictionDto mlPredictionDto) {
        List<TradingSignal> signals = new ArrayList<>();

        if ("BUY".equalsIgnoreCase(mlPredictionDto.getPrediction()) && mlPredictionDto.getConfidence() > 0.7) {
            TradingSignal buySignal = new TradingSignal();
            buySignal.setSymbol(mlPredictionDto.getSymbol());
            buySignal.setTimestamp(mlPredictionDto.getTimestamp());
            buySignal.setSignalType(TradingSignal.SignalType.BUY);
            buySignal.setStrategyName(getName());
            buySignal.setDescription(String.format("ML BUY: Prediction %s with %.2f confidence", mlPredictionDto.getPrediction(), mlPredictionDto.getConfidence()));
            signals.add(buySignal);
        } else if ("SELL".equalsIgnoreCase(mlPredictionDto.getPrediction()) && mlPredictionDto.getConfidence() > 0.7) {
            TradingSignal sellSignal = new TradingSignal();
            sellSignal.setSymbol(mlPredictionDto.getSymbol());
            sellSignal.setTimestamp(mlPredictionDto.getTimestamp());
            sellSignal.setSignalType(TradingSignal.SignalType.SELL);
            sellSignal.setStrategyName(getName());
            sellSignal.setDescription(String.format("ML SELL: Prediction %s with %.2f confidence", mlPredictionDto.getPrediction(), mlPredictionDto.getConfidence()));
            signals.add(sellSignal);
        }
        return signals;
    }

    @Override
    public String getName() {
        return "ML_BASED_STRATEGY";
    }
}
