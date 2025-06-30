package com.example.tradingplatformmvp;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final StockDataRepository stockDataRepository;

    public KafkaConsumerService(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    @KafkaListener(topics = "stock-data-topic", groupId = "trading-platform-group")
    public void consumeStockData(StockDataDto stockDataDto) {
        System.out.println("Received from Kafka: " + stockDataDto.getSymbol() + " - " + stockDataDto.getTimestamp());
        StockData stockData = new StockData();
        stockData.setSymbol(stockDataDto.getSymbol());
        stockData.setTimestamp(stockDataDto.getTimestamp());
        stockData.setOpen(stockDataDto.getOpen());
        stockData.setHigh(stockDataDto.getHigh());
        stockData.setLow(stockDataDto.getLow());
        stockData.setClose(stockDataDto.getClose());
        stockData.setVolume(stockDataDto.getVolume());
        stockDataRepository.save(stockData);
    }
}
