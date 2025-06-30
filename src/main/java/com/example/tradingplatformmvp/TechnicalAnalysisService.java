package com.example.tradingplatformmvp;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TechnicalAnalysisService {

    public double calculateSMA(List<StockData> stockDataList, int barCount) {
        if (stockDataList == null || stockDataList.isEmpty() || stockDataList.size() < barCount) {
            return 0.0; // Not enough data to calculate SMA
        }

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

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, barCount);

        // Get the SMA value for the last bar
        return sma.getValue(series.getEndIndex()).doubleValue();
    }
}
