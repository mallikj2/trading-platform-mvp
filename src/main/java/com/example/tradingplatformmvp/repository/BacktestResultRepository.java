package com.example.tradingplatformmvp.repository;

import com.example.tradingplatformmvp.model.BacktestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {
}