package com.learning.aggregator.service;

import com.google.protobuf.Empty;
import com.leaning.stock.StockServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PriceUpdateSubscriptionInitializer implements CommandLineRunner {

    @GrpcClient("stock-service")
    private StockServiceGrpc.StockServiceStub stockClient;

    private final PriceUpdateListener priceUpdateListener;

    public PriceUpdateSubscriptionInitializer(PriceUpdateListener priceUpdateListener) {
        this.priceUpdateListener = priceUpdateListener;
    }

    @Override
    public void run(String... args) throws Exception {
        this.stockClient
                .withWaitForReady()
                .getPriceUpdates(Empty.getDefaultInstance(), priceUpdateListener);
    }
}
