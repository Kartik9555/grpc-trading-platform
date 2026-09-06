package com.learning.aggregator.controller;

import com.learning.aggregator.service.PriceUpdateListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("stock")
public class StockController {

    private final PriceUpdateListener listener;

    public StockController(PriceUpdateListener listener) {
        this.listener = listener;
    }

    @GetMapping(value = "updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter priceUpdates() {
        return listener.createSseEmitter();
    }
}
