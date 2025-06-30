package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.dto.IndicatorDto;
import com.example.tradingplatformmvp.dto.MlPredictionDto;
import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.repository.StockDataRepository;
import com.example.tradingplatformmvp.repository.TradingSignalRepository;
import com.example.tradingplatformmvp.strategy.SmaCrossoverStrategy;
import com.example.tradingplatformmvp.strategy.TradingStrategy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignalGenerationService {

    private final TradingSignalRepository tradingSignalRepository;
    private final KafkaTemplate<String, TradingSignal> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final StockDataRepository stockDataRepository;
    private final SmaCrossoverStrategy smaCrossoverStrategy; // Inject specific strategy

    public SignalGenerationService(TradingSignalRepository tradingSignalRepository,
                                   KafkaTemplate<String, TradingSignal> kafkaTemplate,
                                   SimpMessagingTemplate messagingTemplate,
                                   StockDataRepository stockDataRepository,
                                   SmaCrossoverStrategy smaCrossoverStrategy) {
        this.tradingSignalRepository = tradingSignalRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
        this.stockDataRepository = stockDataRepository;
        this.smaCrossoverStrategy = smaCrossoverStrategy;
    }

    @KafkaListener(topics = "stock-indicators-topic", groupId = "trading-platform-group")
    public void consumeIndicatorsAndGenerateSignals(IndicatorDto indicatorDto) {
        System.out.println("SignalGenerationService received indicators: " + indicatorDto.getSymbol() + " - " + indicatorDto.getTimestamp());

        // Fetch historical data for the symbol to build the series for strategy
        List<StockData> historicalData = stockDataRepository.findBySymbolOrderByTimestampAsc(indicatorDto.getSymbol());

        // Create a StockData object from the IndicatorDto for strategy processing
        StockData currentStockData = new StockData();
        currentStockData.setSymbol(indicatorDto.getSymbol());
        currentStockData.setTimestamp(indicatorDto.getTimestamp());
        // Note: Open, High, Low, Volume are not in IndicatorDto, so we use close for simplicity
        currentStockData.setClose(indicatorDto.getSma()); // Using SMA as close for strategy input

        List<TradingSignal> signals = smaCrossoverStrategy.generateSignals(historicalData, currentStockData);

        for (TradingSignal signal : signals) {
            tradingSignalRepository.save(signal);
            kafkaTemplate.send("trading-signals-topic", signal.getSymbol(), signal);
            messagingTemplate.convertAndSend("/topic/trading-signals/" + signal.getSymbol(), signal);
            System.out.println("Generated Signal: " + signal.getDescription());
        }
    }

    @KafkaListener(topics = "ml-predictions-topic", groupId = "trading-platform-group")
    public void consumeMlPredictionAndGenerateSignals(MlPredictionDto mlPredictionDto) {
        System.out.println("SignalGenerationService received ML Prediction: " + mlPredictionDto.getSymbol() + " - " + mlPredictionDto.getPrediction());

        // Example: Generate a BUY signal if ML model predicts "BUY" with high confidence
        if ("BUY".equalsIgnoreCase(mlPredictionDto.getPrediction()) && mlPredictionDto.getConfidence() > 0.7) {
            TradingSignal buySignal = new TradingSignal();
            buySignal.setSymbol(mlPredictionDto.getSymbol());
            buySignal.setTimestamp(mlPredictionDto.getTimestamp());
            buySignal.setSignalType(TradingSignal.SignalType.BUY);
            buySignal.setStrategyName("ML_PREDICTION");
            buySignal.setDescription(String.format("ML BUY: Prediction %s with %.2f confidence", mlPredictionDto.getPrediction(), mlPredictionDto.getConfidence()));
            tradingSignalRepository.save(buySignal);
            kafkaTemplate.send("trading-signals-topic", buySignal.getSymbol(), buySignal);
            messagingTemplate.convertAndSend("/topic/trading-signals/" + buySignal.getSymbol(), buySignal);
            System.out.println("Generated ML BUY Signal: " + buySignal.getDescription());
        } else if ("SELL".equalsIgnoreCase(mlPredictionDto.getPrediction()) && mlPredictionDto.getConfidence() > 0.7) {
            TradingSignal sellSignal = new TradingSignal();
            sellSignal.setSymbol(mlPredictionDto.getSymbol());
            sellSignal.setTimestamp(mlPredictionDto.getTimestamp());
            sellSignal.setSignalType(TradingSignal.SignalType.SELL);
            sellSignal.setStrategyName("ML_PREDICTION");
            sellSignal.setDescription(String.format("ML SELL: Prediction %s with %.2f confidence", mlPredictionDto.getPrediction(), mlPredictionDto.getConfidence()));
            tradingSignalRepository.save(sellSignal);
            kafkaTemplate.send("trading-signals-topic", sellSignal.getSymbol(), sellSignal);
            messagingTemplate.convertAndSend("/topic/trading-signals/" + sellSignal.getSymbol(), sellSignal);
            System.out.println("Generated ML SELL Signal: " + sellSignal.getDescription());
        }
    }
}

