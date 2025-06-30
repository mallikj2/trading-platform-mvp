package com.example.tradingplatformmvp.repository;

import com.example.tradingplatformmvp.model.SimulatedTrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulatedTradeRepository extends JpaRepository<SimulatedTrade, Long> {
}
