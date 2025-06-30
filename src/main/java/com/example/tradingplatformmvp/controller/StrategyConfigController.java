package com.example.tradingplatformmvp.controller;

import com.example.tradingplatformmvp.model.TradingStrategyConfig;
import com.example.tradingplatformmvp.service.StrategyConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/strategies")
public class StrategyConfigController {

    private final StrategyConfigService strategyConfigService;

    public StrategyConfigController(StrategyConfigService strategyConfigService) {
        this.strategyConfigService = strategyConfigService;
    }

    @PostMapping
    public ResponseEntity<TradingStrategyConfig> createStrategy(@RequestBody TradingStrategyConfig strategyConfig) {
        TradingStrategyConfig savedConfig = strategyConfigService.saveStrategy(strategyConfig);
        return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
    }

    @GetMapping
    public List<TradingStrategyConfig> getAllStrategies() {
        return strategyConfigService.getAllStrategies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradingStrategyConfig> getStrategyById(@PathVariable Long id) {
        return strategyConfigService.getStrategyById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TradingStrategyConfig> updateStrategy(@PathVariable Long id, @RequestBody TradingStrategyConfig strategyConfig) {
        return strategyConfigService.getStrategyById(id)
                .map(existingConfig -> {
                    strategyConfig.setId(id);
                    return new ResponseEntity<>(strategyConfigService.saveStrategy(strategyConfig), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        strategyConfigService.deleteStrategy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/name/{strategyName}/symbol/{symbol}")
    public ResponseEntity<TradingStrategyConfig> getStrategyByNameAndSymbol(
            @PathVariable String strategyName,
            @PathVariable String symbol) {
        TradingStrategyConfig config = strategyConfigService.getStrategyByNameAndSymbol(strategyName, symbol);
        if (config != null) {
            return new ResponseEntity<>(config, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
