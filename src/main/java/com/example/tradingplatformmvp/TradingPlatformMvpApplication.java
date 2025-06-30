package com.example.tradingplatformmvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TradingPlatformMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingPlatformMvpApplication.class, args);
    }

}