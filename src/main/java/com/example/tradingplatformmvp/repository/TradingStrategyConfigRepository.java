package com.example.tradingplatformmvp.repository;

import com.example.tradingplatformmvp.model.TradingStrategyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingStrategyConfigRepository extends JpaRepository<TradingStrategyConfig, Long> {
    TradingStrategyConfig findByStrategyNameAndSymbol(String strategyName, String symbol);
}
