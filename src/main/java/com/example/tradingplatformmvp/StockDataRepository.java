package com.example.tradingplatformmvp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findBySymbolOrderByTimestampAsc(String symbol);
}
