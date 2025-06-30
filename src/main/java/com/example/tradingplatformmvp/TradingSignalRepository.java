package com.example.tradingplatformmvp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {
}
