package com.learning.aggregator.test.mockservice;

import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.leaning.user.UserInformation;
import com.leaning.user.UserInformationRequest;
import com.leaning.user.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class UserMockService extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUserInformation(UserInformationRequest request, StreamObserver<UserInformation> responseObserver) {
        if(request.getUserId() == 1) {
            var response = UserInformation.newBuilder()
                    .setUserId(1)
                    .setName("integration-test")
                    .setBalance(100)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    @Override
    public void tradeStock(StockTradeRequest request, StreamObserver<StockTradeResponse> responseObserver) {
        var response = StockTradeResponse.newBuilder()
                .setUserId(request.getUserId())
                .setTicker(request.getTicker())
                .setAction(request.getAction())
                .setPrice(request.getPrice())
                .setQuantity(request.getQuantity())
                .setBalance(0)
                .setTotalPrice(1000)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
