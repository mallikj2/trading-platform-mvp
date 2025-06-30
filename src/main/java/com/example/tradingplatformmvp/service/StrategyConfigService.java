package com.example.tradingplatformmvp.service;

import com.example.tradingplatformmvp.model.TradingStrategyConfig;
import com.example.tradingplatformmvp.repository.TradingStrategyConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StrategyConfigService {

    private final TradingStrategyConfigRepository repository;

    public StrategyConfigService(TradingStrategyConfigRepository repository) {
        this.repository = repository;
    }

    public List<TradingStrategyConfig> getAllStrategies() {
        return repository.findAll();
    }

    public Optional<TradingStrategyConfig> getStrategyById(Long id) {
        return repository.findById(id);
    }

    public TradingStrategyConfig saveStrategy(TradingStrategyConfig strategyConfig) {
        return repository.save(strategyConfig);
    }

    public void deleteStrategy(Long id) {
        repository.deleteById(id);
    }

    public TradingStrategyConfig getStrategyByNameAndSymbol(String strategyName, String symbol) {
        return repository.findByStrategyNameAndSymbol(strategyName, symbol);
    }
}
