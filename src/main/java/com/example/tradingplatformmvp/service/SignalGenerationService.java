package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.dto.IndicatorDto;
import com.example.tradingplatformmvp.dto.MlPredictionDto;
import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.model.TradingSignal;
import com.example.tradingplatformmvp.model.TradingStrategyConfig;
import com.example.tradingplatformmvp.repository.StockDataRepository;
import com.example.tradingplatformmvp.repository.TradingSignalRepository;
import com.example.tradingplatformmvp.strategy.MlBasedStrategy;
import com.example.tradingplatformmvp.strategy.SmaCrossoverStrategy;
import com.example.tradingplatformmvp.strategy.TradingStrategy;
import org.springframework.context.ApplicationContext;
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
    private final StrategyConfigService strategyConfigService;
    private final ApplicationContext applicationContext; // To get strategy beans dynamically
    private final MlBasedStrategy mlBasedStrategy;

    public SignalGenerationService(TradingSignalRepository tradingSignalRepository,
                                   KafkaTemplate<String, TradingSignal> kafkaTemplate,
                                   SimpMessagingTemplate messagingTemplate,
                                   StockDataRepository stockDataRepository,
                                   StrategyConfigService strategyConfigService,
                                   ApplicationContext applicationContext,
                                   MlBasedStrategy mlBasedStrategy) {
        this.tradingSignalRepository = tradingSignalRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
        this.stockDataRepository = stockDataRepository;
        this.strategyConfigService = strategyConfigService;
        this.applicationContext = applicationContext;
        this.mlBasedStrategy = mlBasedStrategy;
    }

    @KafkaListener(topics = "stock-indicators-topic", groupId = "trading-platform-group")
    public void consumeIndicatorsAndGenerateSignals(IndicatorDto indicatorDto) {
        System.out.println("SignalGenerationService received indicators: " + indicatorDto.getSymbol() + " - " + indicatorDto.getTimestamp());

        List<TradingStrategyConfig> enabledStrategies = strategyConfigService.getAllStrategies();

        for (TradingStrategyConfig config : enabledStrategies) {
            if (config.isEnabled() && config.getSymbol().equals(indicatorDto.getSymbol())) {
                try {
                    // Dynamically get the strategy bean by name
                    TradingStrategy strategy = (TradingStrategy) applicationContext.getBean(config.getStrategyName().toLowerCase() + "Strategy");

                    // Fetch historical data for the symbol to build the series for strategy
                    List<StockData> historicalData = stockDataRepository.findBySymbolOrderByTimestampAsc(indicatorDto.getSymbol());

                    // Create a StockData object from the IndicatorDto for strategy processing
                    StockData currentStockData = new StockData();
                    currentStockData.setSymbol(indicatorDto.getSymbol());
                    currentStockData.setTimestamp(indicatorDto.getTimestamp());
                    currentStockData.setOpen(indicatorDto.getSma()); // Placeholder, ideally actual open/high/low/close
                    currentStockData.setHigh(indicatorDto.getSma());
                    currentStockData.setLow(indicatorDto.getSma());
                    currentStockData.setClose(indicatorDto.getSma()); // Using SMA as close for strategy input
                    currentStockData.setVolume(0); // Placeholder

                    List<TradingSignal> signals = strategy.generateSignals(historicalData, currentStockData, config.getParameters());

                    for (TradingSignal signal : signals) {
                        tradingSignalRepository.save(signal);
                        kafkaTemplate.send("trading-signals-topic", signal.getSymbol(), signal);
                        messagingTemplate.convertAndSend("/topic/trading-signals/" + signal.getSymbol(), signal);
                        System.out.println("Generated Signal: " + signal.getDescription());
                    }
                } catch (Exception e) {
                    System.err.println("Error applying strategy " + config.getStrategyName() + ": " + e.getMessage());
                }
            }
        }
    }

    @KafkaListener(topics = "ml-predictions-topic", groupId = "trading-platform-group")
    public void consumeMlPredictionAndGenerateSignals(MlPredictionDto mlPredictionDto) {
        System.out.println("SignalGenerationService received ML Prediction: " + mlPredictionDto.getSymbol() + " - " + mlPredictionDto.getPrediction());

        List<TradingSignal> signals = mlBasedStrategy.generateSignalsFromMlPrediction(mlPredictionDto);

        for (TradingSignal signal : signals) {
            tradingSignalRepository.save(signal);
            kafkaTemplate.send("trading-signals-topic", signal.getSymbol(), signal);
            messagingTemplate.convertAndSend("/topic/trading-signals/" + signal.getSymbol(), signal);
            System.out.println("Generated ML Signal: " + signal.getDescription());
        }
    }
}
