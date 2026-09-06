package com.learning.aggregator.service;

import com.leaning.stock.StockPriceRequest;
import com.leaning.stock.StockServiceGrpc;
import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.leaning.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userClient;

    @GrpcClient("stock-service")
    private StockServiceGrpc.StockServiceBlockingStub stockClient;

    public StockTradeResponse trade(StockTradeRequest request){
        var priceRequest = StockPriceRequest.newBuilder()
                .setTicker(request.getTicker())
                .build();
        var priceResponse = this.stockClient.getStockPrice(priceRequest);
        var tradeRequest = request.toBuilder().setPrice(priceResponse.getPrice()).build();
        return this.userClient.tradeStock(tradeRequest);
    }
}
