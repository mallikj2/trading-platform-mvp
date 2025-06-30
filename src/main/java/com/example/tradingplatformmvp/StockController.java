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

    public StockController(StockDataRepository stockDataRepository, TechnicalAnalysisService technicalAnalysisService) {
        this.stockDataRepository = stockDataRepository;
        this.technicalAnalysisService = technicalAnalysisService;
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
}
