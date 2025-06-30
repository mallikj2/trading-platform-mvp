package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.dto.StockDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DataIngestionService {

    private final KafkaTemplate<String, StockDataDto> kafkaTemplate;
    private final WebClient webClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    public DataIngestionService(KafkaTemplate<String, StockDataDto> kafkaTemplate, WebClient.Builder webClientBuilder, SimpMessagingTemplate messagingTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.messagingTemplate = messagingTemplate;
    }

    @Retryable(value = {WebClientResponseException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void fetchAndPublishStockData(String symbol) {
        String url = String.format("/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s", symbol, apiKey);

        webClient.get().uri(url).retrieve().bodyToMono(Map.class)
                .subscribe(response -> {
                    Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (1min)");
                    if (timeSeries != null) {
                        timeSeries.forEach((timestampStr, data) -> {
                            Map<String, String> stockDataMap = (Map<String, String>) data;
                            StockDataDto stockDataDto = new StockDataDto();
                            stockDataDto.setSymbol(symbol);
                            stockDataDto.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            stockDataDto.setOpen(Double.parseDouble(stockDataMap.get("1. open")));
                            stockDataDto.setHigh(Double.parseDouble(stockDataMap.get("2. high")));
                            stockDataDto.setLow(Double.parseDouble(stockDataMap.get("3. low")));
                            stockDataDto.setClose(Double.parseDouble(stockDataMap.get("4. close")));
                            stockDataDto.setVolume(Long.parseLong(stockDataMap.get("5. volume")));

                            kafkaTemplate.send("stock-data-topic", stockDataDto.getSymbol(), stockDataDto);
                            messagingTemplate.convertAndSend("/topic/stock-data/" + stockDataDto.getSymbol(), stockDataDto);
                            System.out.println("Sent to Kafka and WebSocket: " + stockDataDto.getSymbol() + " - " + stockDataDto.getTimestamp());
                        });
                    } else {
                        System.err.println("Error fetching data for " + symbol + ": " + response.get("Note"));
                    }
                }, error -> {
                    System.err.println("Error during Alpha Vantage API call for " + symbol + ": " + error.getMessage());
                    // Re-throw to trigger retry
                    throw new WebClientResponseException(error.getMessage(), 500, "Internal Server Error", null, null, null);
                });
    }
}
