package com.example.tradingplatformmvp;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BacktestingService {

    private final StockDataRepository stockDataRepository;
    private final TechnicalAnalysisService technicalAnalysisService;
    private final BacktestResultRepository backtestResultRepository;

    public BacktestingService(StockDataRepository stockDataRepository,
                              TechnicalAnalysisService technicalAnalysisService,
                              BacktestResultRepository backtestResultRepository) {
        this.stockDataRepository = stockDataRepository;
        this.technicalAnalysisService = technicalAnalysisService;
        this.backtestResultRepository = backtestResultRepository;
    }

    public BacktestResult runSmaCrossoverBacktest(
            String symbol,
            LocalDate startDate,
            LocalDate endDate,
            double initialCapital,
            int shortSmaPeriod,
            int longSmaPeriod) {

        List<StockData> historicalData = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);

        // Filter data for the specified date range
        List<StockData> filteredData = historicalData.stream()
                .filter(data -> !data.getTimestamp().toLocalDate().isBefore(startDate) &&
                                 !data.getTimestamp().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        if (filteredData.size() < longSmaPeriod) {
            return createInsufficientDataResult(symbol, startDate, endDate, initialCapital, "SMA_CROSSOVER");
        }

        BarSeries series = technicalAnalysisService.buildBarSeries(filteredData);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaPeriod);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaPeriod);

        double currentCapital = initialCapital;
        boolean inTrade = false;
        double buyPrice = 0.0;
        int totalTrades = 0;
        int winningTrades = 0;
        int losingTrades = 0;

        for (int i = longSmaPeriod - 1; i < series.getBarCount(); i++) {
            double currentShortSma = shortSma.getValue(i).doubleValue();
            double currentLongSma = longSma.getValue(i).doubleValue();
            double previousShortSma = shortSma.getValue(i - 1).doubleValue();
            double previousLongSma = longSma.getValue(i - 1).doubleValue();
            double currentClosePrice = series.getBar(i).getClosePrice().doubleValue();

            // Buy signal: Short SMA crosses above Long SMA
            if (currentShortSma > currentLongSma && previousShortSma <= previousLongSma && !inTrade) {
                buyPrice = currentClosePrice;
                inTrade = true;
                // System.out.println("BUY at " + buyPrice + " on " + series.getBar(i).getEndTime());
            }
            // Sell signal: Short SMA crosses below Long SMA
            else if (currentShortSma < currentLongSma && previousShortSma >= previousLongSma && inTrade) {
                double sellPrice = currentClosePrice;
                double profitLoss = sellPrice - buyPrice;
                currentCapital += profitLoss; // Assuming 1 unit trade
                totalTrades++;
                if (profitLoss > 0) {
                    winningTrades++;
                } else {
                    losingTrades++;
                }
                inTrade = false;
                // System.out.println("SELL at " + sellPrice + " on " + series.getBar(i).getEndTime() + " P/L: " + profitLoss);
            }
        }

        // If still in trade at the end, close the trade at the last close price
        if (inTrade) {
            double sellPrice = series.getBar(series.getEndIndex()).getClosePrice().doubleValue();
            double profitLoss = sellPrice - buyPrice;
            currentCapital += profitLoss;
            totalTrades++;
            if (profitLoss > 0) {
                winningTrades++;
            } else {
                losingTrades++;
            }
        }

        double totalProfitLoss = currentCapital - initialCapital;
        double percentageProfitLoss = (totalProfitLoss / initialCapital) * 100;

        BacktestResult result = new BacktestResult();
        result.setStrategyName("SMA_CROSSOVER");
        result.setSymbol(symbol);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setInitialCapital(initialCapital);
        result.setFinalCapital(currentCapital);
        result.setTotalProfitLoss(totalProfitLoss);
        result.setPercentageProfitLoss(percentageProfitLoss);
        result.setTotalTrades(totalTrades);
        result.setWinningTrades(winningTrades);
        result.setLosingTrades(losingTrades);
        result.setBacktestRunTime(LocalDateTime.now());

        return backtestResultRepository.save(result);
    }

    private BacktestResult createInsufficientDataResult(String symbol, LocalDate startDate, LocalDate endDate, double initialCapital, String strategyName) {
        BacktestResult result = new BacktestResult();
        result.setStrategyName(strategyName);
        result.setSymbol(symbol);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setInitialCapital(initialCapital);
        result.setFinalCapital(initialCapital);
        result.setTotalProfitLoss(0.0);
        result.setPercentageProfitLoss(0.0);
        result.setTotalTrades(0);
        result.setWinningTrades(0);
        result.setLosingTrades(0);
        result.setDescription("Insufficient data for backtesting.");
        result.setBacktestRunTime(LocalDateTime.now());
        return backtestResultRepository.save(result);
    }
}
