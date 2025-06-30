package com.example.tradingplatformmvp;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignalGenerationService {

    private final TechnicalAnalysisService technicalAnalysisService;
    private final TradingSignalRepository tradingSignalRepository;
    private final KafkaTemplate<String, TradingSignal> kafkaTemplate;
    private final StockDataRepository stockDataRepository;

    // Store last SMA values for each symbol to detect crossovers
    private final ConcurrentHashMap<String, Double> lastShortSma = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> lastLongSma = new ConcurrentHashMap<>();

    public SignalGenerationService(TechnicalAnalysisService technicalAnalysisService,
                                   TradingSignalRepository tradingSignalRepository,
                                   KafkaTemplate<String, TradingSignal> kafkaTemplate,
                                   StockDataRepository stockDataRepository) {
        this.technicalAnalysisService = technicalAnalysisService;
        this.tradingSignalRepository = tradingSignalRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.stockDataRepository = stockDataRepository;
    }

    @KafkaListener(topics = "stock-data-topic", groupId = "trading-platform-group")
    public void consumeStockDataAndGenerateSignals(StockDataDto stockDataDto) {
        System.out.println("SignalGenerationService received: " + stockDataDto.getSymbol() + " - " + stockDataDto.getTimestamp());

        // Fetch all historical data for the symbol to build the series
        List<StockData> stockDataList = stockDataRepository.findBySymbolOrderByTimestampAsc(stockDataDto.getSymbol());

        // Add the current incoming data point to the list for analysis
        StockData currentStockData = new StockData();
        currentStockData.setSymbol(stockDataDto.getSymbol());
        currentStockData.setTimestamp(stockDataDto.getTimestamp());
        currentStockData.setOpen(stockDataDto.getOpen());
        currentStockData.setHigh(stockDataDto.getHigh());
        currentStockData.setLow(stockDataDto.getLow());
        currentStockData.setClose(stockDataDto.getClose());
        currentStockData.setVolume(stockDataDto.getVolume());
        stockDataList.add(currentStockData);

        // Ensure enough data for SMA calculation
        int shortSmaPeriod = 5; // Example short period
        int longSmaPeriod = 20; // Example long period

        if (stockDataList.size() < longSmaPeriod) {
            System.out.println("Not enough data for SMA crossover strategy for " + stockDataDto.getSymbol());
            return;
        }

        BarSeries series = technicalAnalysisService.buildBarSeries(stockDataList);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaPeriod);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaPeriod);

        double currentShortSma = shortSma.getValue(series.getEndIndex()).doubleValue();
        double currentLongSma = longSma.getValue(series.getEndIndex()).doubleValue();

        Double prevShortSma = lastShortSma.get(stockDataDto.getSymbol());
        Double prevLongSma = lastLongSma.get(stockDataDto.getSymbol());

        // Update last SMA values
        lastShortSma.put(stockDataDto.getSymbol(), currentShortSma);
        lastLongSma.put(stockDataDto.getSymbol(), currentLongSma);

        // Generate signals based on SMA crossover
        if (prevShortSma != null && prevLongSma != null) {
            // Buy signal: Short SMA crosses above Long SMA
            if (currentShortSma > currentLongSma && prevShortSma <= prevLongSma) {
                TradingSignal buySignal = new TradingSignal();
                buySignal.setSymbol(stockDataDto.getSymbol());
                buySignal.setTimestamp(stockDataDto.getTimestamp());
                buySignal.setSignalType(TradingSignal.SignalType.BUY);
                buySignal.setStrategyName("SMA_CROSSOVER");
                buySignal.setDescription(String.format("BUY: Short SMA (%.2f) crossed above Long SMA (%.2f)", currentShortSma, currentLongSma));
                tradingSignalRepository.save(buySignal);
                kafkaTemplate.send("trading-signals-topic", buySignal.getSymbol(), buySignal);
                System.out.println("Generated BUY Signal: " + buySignal.getDescription());
            }
            // Sell signal: Short SMA crosses below Long SMA
            else if (currentShortSma < currentLongSma && prevShortSma >= prevLongSma) {
                TradingSignal sellSignal = new TradingSignal();
                sellSignal.setSymbol(stockDataDto.getSymbol());
                sellSignal.setTimestamp(stockDataDto.getTimestamp());
                sellSignal.setSignalType(TradingSignal.SignalType.SELL);
                sellSignal.setStrategyName("SMA_CROSSOVER");
                sellSignal.setDescription(String.format("SELL: Short SMA (%.2f) crossed below Long SMA (%.2f)", currentShortSma, currentLongSma));
                tradingSignalRepository.save(sellSignal);
                kafkaTemplate.send("trading-signals-topic", sellSignal.getSymbol(), sellSignal);
                System.out.println("Generated SELL Signal: " + sellSignal.getDescription());
            }
        }
    }
}
