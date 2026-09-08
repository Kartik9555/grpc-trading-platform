package com.learning.stock.service;

import com.google.protobuf.Empty;
import com.leaning.stock.PriceUpdate;
import com.leaning.stock.StockPriceRequest;
import com.leaning.stock.StockPriceResponse;
import com.leaning.stock.StockServiceGrpc;
import com.learning.stock.service.event.StockPriceEvent;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@GrpcService
public class StockServiceImpl extends StockServiceGrpc.StockServiceImplBase implements ApplicationListener<StockPriceEvent> {
    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);
    private final Tickers tickers;
    private final Set<ServerCallStreamObserver<PriceUpdate>> set;

    public StockServiceImpl(Tickers tickers) {
        this.tickers = tickers;
        this.set = Collections.synchronizedSet(new HashSet<>());
    }

    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {
        this.tickers.getPrice(request.getTicker()).map((v) -> StockPriceResponse.newBuilder().setTicker(request.getTicker()).setPrice(v).build()).ifPresentOrElse((v) -> {
            responseObserver.onNext(v);
            responseObserver.onCompleted();
        }, () -> responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(request.getTicker() + " is not valid").asRuntimeException()));
    }

    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {
        ServerCallStreamObserver<PriceUpdate> o = (ServerCallStreamObserver<PriceUpdate>)responseObserver;
        this.set.add(o);
        o.setOnCancelHandler(() -> this.cancel(o));
        o.setOnCloseHandler(() -> this.cancel(o));
    }

    public void onApplicationEvent(@NonNull StockPriceEvent event) {
        for(ServerCallStreamObserver<PriceUpdate> o : this.set) {
            o.onNext(event.getPriceUpdate());
        }

    }

    private void cancel(ServerCallStreamObserver<PriceUpdate> o) {
        log.info("price updates observer cancelled");
        this.set.remove(o);
    }
}

