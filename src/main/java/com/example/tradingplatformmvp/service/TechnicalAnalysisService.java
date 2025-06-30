package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.dto.IndicatorDto;
import com.example.tradingplatformmvp.dto.StockDataDto;
import com.example.tradingplatformmvp.model.StockData;
import com.example.tradingplatformmvp.repository.StockDataRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.EMAIndicator;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TechnicalAnalysisService {

    private final StockDataRepository stockDataRepository;
    private final KafkaTemplate<String, IndicatorDto> kafkaTemplate;

    public TechnicalAnalysisService(StockDataRepository stockDataRepository, KafkaTemplate<String, IndicatorDto> kafkaTemplate) {
        this.stockDataRepository = stockDataRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public BarSeries buildBarSeries(List<StockData> stockDataList) {
        BarSeries series = new BaseBarSeriesBuilder().withName("stock_series").build();
        for (StockData data : stockDataList) {
            series.addBar(
                Duration.ofMinutes(1), // Assuming 1-minute bars for simplicity
                ZonedDateTime.of(data.getTimestamp(), ZoneId.systemDefault()),
                data.getOpen(),
                data.getHigh(),
                data.getLow(),
                data.getClose(),
                data.getVolume()
            );
        }
        return series;
    }

    public double calculateSMA(List<StockData> stockDataList, int barCount) {
        if (stockDataList == null || stockDataList.isEmpty() || stockDataList.size() < barCount) {
            return 0.0; // Not enough data to calculate SMA
        }
        BarSeries series = buildBarSeries(stockDataList);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, barCount);
        return sma.getValue(series.getEndIndex()).doubleValue();
    }

    public double calculateRSI(List<StockData> stockDataList, int barCount) {
        if (stockDataList == null || stockDataList.isEmpty() || stockDataList.size() < barCount) {
            return 0.0; // Not enough data to calculate RSI
        }
        BarSeries series = buildBarSeries(stockDataList);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, barCount);
        return rsi.getValue(series.getEndIndex()).doubleValue();
    }

    public double[] calculateMACD(List<StockData> stockDataList, int fastBarCount, int slowBarCount, int signalBarCount) {
        if (stockDataList == null || stockDataList.isEmpty() || stockDataList.size() < Math.max(fastBarCount, slowBarCount)) {
            return new double[]{0.0, 0.0, 0.0}; // Not enough data to calculate MACD
        }
        BarSeries series = buildBarSeries(stockDataList);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator emaFast = new EMAIndicator(closePrice, fastBarCount);
        EMAIndicator emaSlow = new EMAIndicator(closePrice, slowBarCount);

        MACDIndicator macd = new MACDIndicator(closePrice, fastBarCount, slowBarCount);
        EMAIndicator signal = new EMAIndicator(macd, signalBarCount);

        double macdValue = macd.getValue(series.getEndIndex()).doubleValue();
        double signalValue = signal.getValue(series.getEndIndex()).doubleValue();
        double histogramValue = macdValue - signalValue;

        return new double[]{macdValue, signalValue, histogramValue};
    }

    @KafkaListener(topics = "stock-data-topic", groupId = "trading-platform-group")
    public void consumeStockDataAndPublishIndicators(StockDataDto stockDataDto) {
        System.out.println("TechnicalAnalysisService received: " + stockDataDto.getSymbol() + " - " + stockDataDto.getTimestamp());

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

        // Ensure enough data for calculations
        if (stockDataList.size() < 20) { // Minimum bars for some indicators
            System.out.println("Not enough data for indicator calculation for " + stockDataDto.getSymbol());
            return;
        }

        double sma = calculateSMA(stockDataList, 20); // Example SMA period
        double rsi = calculateRSI(stockDataList, 14); // Example RSI period
        double[] macdValues = calculateMACD(stockDataList, 12, 26, 9); // Example MACD periods

        IndicatorDto indicatorDto = new IndicatorDto();
        indicatorDto.setSymbol(stockDataDto.getSymbol());
        indicatorDto.setTimestamp(stockDataDto.getTimestamp());
        indicatorDto.setSma(sma);
        indicatorDto.setRsi(rsi);
        indicatorDto.setMacd(macdValues[0]);
        indicatorDto.setMacdSignal(macdValues[1]);
        indicatorDto.setMacdHist(macdValues[2]);

        kafkaTemplate.send("stock-indicators-topic", indicatorDto.getSymbol(), indicatorDto);
        System.out.println("Published indicators to Kafka: " + indicatorDto.getSymbol() + " - " + indicatorDto.getTimestamp());
    }
}