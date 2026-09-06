package com.learning.aggregator.controller;

import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.learning.aggregator.service.TradeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public StockTradeResponse trade(@RequestBody StockTradeRequest request) {
        return tradeService.trade(request);
    }
}
