package com.example.tradingplatformmvp.repository;

import com.example.tradingplatformmvp.model.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findBySymbolOrderByTimestampAsc(String symbol);
}