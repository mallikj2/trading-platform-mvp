package com.example.tradingplatformmvp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockDataRepository stockDataRepository;
    private final TechnicalAnalysisService technicalAnalysisService;
    private final DataIngestionService dataIngestionService;

    public StockController(StockDataRepository stockDataRepository, TechnicalAnalysisService technicalAnalysisService, DataIngestionService dataIngestionService) {
        this.stockDataRepository = stockDataRepository;
        this.technicalAnalysisService = technicalAnalysisService;
        this.dataIngestionService = dataIngestionService;
    }

    @GetMapping("/{symbol}")
    public List<StockData> getStockData(@PathVariable String symbol) {
        return stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);
    }

    @GetMapping("/{symbol}/sma/{barCount}")
    public String getSMA(@PathVariable String symbol, @PathVariable int barCount) {
        List<StockData> stockDataList = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);
        if (stockDataList.isEmpty()) {
            return "No data found for symbol: " + symbol;
        }
        if (stockDataList.size() < barCount) {
            return "Not enough data (" + stockDataList.size() + " bars) to calculate SMA for " + barCount + " bars.";
        }
        double sma = technicalAnalysisService.calculateSMA(stockDataList, barCount);
        return String.format("SMA for %s over %d bars: %.2f", symbol, barCount, sma);
    }

    @GetMapping("/{symbol}/rsi/{barCount}")
    public String getRSI(@PathVariable String symbol, @PathVariable int barCount) {
        List<StockData> stockDataList = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);
        if (stockDataList.isEmpty()) {
            return "No data found for symbol: " + symbol;
        }
        if (stockDataList.size() < barCount) {
            return "Not enough data (" + stockDataList.size() + " bars) to calculate RSI for " + barCount + " bars.";
        }
        double rsi = technicalAnalysisService.calculateRSI(stockDataList, barCount);
        return String.format("RSI for %s over %d bars: %.2f", symbol, barCount, rsi);
    }

    @GetMapping("/{symbol}/macd/{fastBarCount}/{slowBarCount}/{signalBarCount}")
    public String getMACD(
            @PathVariable String symbol,
            @PathVariable int fastBarCount,
            @PathVariable int slowBarCount,
            @PathVariable int signalBarCount) {
        List<StockData> stockDataList = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);
        if (stockDataList.isEmpty()) {
            return "No data found for symbol: " + symbol;
        }
        if (stockDataList.size() < Math.max(fastBarCount, slowBarCount)) {
            return "Not enough data (" + stockDataList.size() + " bars) to calculate MACD.";
        }
        double[] macdValues = technicalAnalysisService.calculateMACD(stockDataList, fastBarCount, slowBarCount, signalBarCount);
        return String.format("MACD for %s (MACD: %.2f, Signal: %.2f, Histogram: %.2f)",
                symbol, macdValues[0], macdValues[1], macdValues[2]);
    }

    @GetMapping("/{symbol}/fetch")
    public String fetchStockData(@PathVariable String symbol) {
        dataIngestionService.fetchAndPublishStockData(symbol);
        return "Fetching and publishing data for " + symbol + ". Check console for Kafka messages.";
    }
}


