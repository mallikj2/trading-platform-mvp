package com.example.tradingplatformmvp.repository;

import com.example.tradingplatformmvp.model.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {
}