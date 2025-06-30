package com.example.tradingplatformmvp;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class DataIngestionService {

    private final StockDataRepository stockDataRepository;

    public DataIngestionService(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    @PostConstruct
    public void init() {
        // Simulate historical data for AAPL
        if (stockDataRepository.count() == 0) {
            StockData s1 = new StockData();
            s1.setSymbol("AAPL");
            s1.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 30));
            s1.setOpen(170.0);
            s1.setHigh(171.0);
            s1.setLow(169.0);
            s1.setClose(170.5);
            s1.setVolume(100000);

            StockData s2 = new StockData();
            s2.setSymbol("AAPL");
            s2.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 31));
            s2.setOpen(170.5);
            s2.setHigh(171.5);
            s2.setLow(170.0);
            s2.setClose(171.2);
            s2.setVolume(120000);

            StockData s3 = new StockData();
            s3.setSymbol("AAPL");
            s3.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 32));
            s3.setOpen(171.2);
            s3.setHigh(172.0);
            s3.setLow(171.0);
            s3.setClose(171.8);
            s3.setVolume(110000);

            StockData s4 = new StockData();
            s4.setSymbol("AAPL");
            s4.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 33));
            s4.setOpen(171.8);
            s4.setHigh(172.5);
            s4.setLow(171.5);
            s4.setClose(172.3);
            s4.setVolume(130000);

            StockData s5 = new StockData();
            s5.setSymbol("AAPL");
            s5.setTimestamp(LocalDateTime.of(2024, 1, 1, 9, 34));
            s5.setOpen(172.3);
            s5.setHigh(173.0);
            s5.setLow(172.0);
            s5.setClose(172.8);
            s5.setVolume(140000);

            stockDataRepository.saveAll(Arrays.asList(s1, s2, s3, s4, s5));
        }
    }
}
