package com.learning.aggregator.service;

import com.leaning.stock.PriceUpdate;
import com.learning.aggregator.dto.PriceUpdateDto;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class PriceUpdateListener implements StreamObserver<PriceUpdate> {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateListener.class);
    private final Set<SseEmitter> emitters = Collections.synchronizedSet(new HashSet<>());
    private final Long sseTimeout;

    public PriceUpdateListener(@Value("${sse.timeout:300000}") Long sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    public SseEmitter createSseEmitter() {
        var emitter = new SseEmitter(sseTimeout);
        emitters.add(emitter);
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void onNext(PriceUpdate priceUpdate) {
        var dto = new PriceUpdateDto(priceUpdate.getTicker().toString(), priceUpdate.getPrice());
        this.emitters.removeIf(emitter -> !this.send(emitter, dto));
    }

    @Override
    public void onError(Throwable throwable) {
        log.warn("Streaming error", throwable);
        this.emitters.forEach(emitter -> emitter.completeWithError(throwable));
        this.emitters.clear();
    }

    @Override
    public void onCompleted() {
        this.emitters.forEach(ResponseBodyEmitter::complete);
        this.emitters.clear();
    }

    private boolean send(SseEmitter emitter, Object obj) {
        try {
            emitter.send(obj);
            return true;
        } catch (IOException exception) {
            log.warn("Failed to send event to emitter: {}", exception.getMessage());
            return false;
        }
    }
}
