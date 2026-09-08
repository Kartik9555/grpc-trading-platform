package com.learning.aggregator.test.mockservice;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import com.leaning.common.Ticker;
import com.leaning.stock.PriceUpdate;
import com.leaning.stock.StockPriceRequest;
import com.leaning.stock.StockPriceResponse;
import com.leaning.stock.StockServiceGrpc;
import com.learning.aggregator.test.StockUpdatesTest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class StockMockService extends StockServiceGrpc.StockServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StockMockService.class);
    @Override
    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {
        var response = StockPriceResponse.newBuilder()
                .setPrice(15)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        for (int i = 1; i <= 5 ; i++) {
            var priceUpdate = PriceUpdate.newBuilder()
                    .setTicker(Ticker.AMAZON)
                    .setPrice(i)
                    .build();
            log.info("Price update received: {}", priceUpdate);
            responseObserver.onNext(priceUpdate);
        }
        responseObserver.onCompleted();
    }
}
