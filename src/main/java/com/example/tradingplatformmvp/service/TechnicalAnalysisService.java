package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.model.StockData;
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
}
