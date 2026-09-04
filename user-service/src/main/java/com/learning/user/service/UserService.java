package com.learning.user.service;

import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.leaning.user.UserInformation;
import com.leaning.user.UserInformationRequest;
import com.leaning.user.UserServiceGrpc;
import com.learning.user.service.handler.UserInformationRequestHandler;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase{

    private final UserInformationRequestHandler userRequestHandler;

    public UserService(UserInformationRequestHandler userRequestHandler) {
        this.userRequestHandler = userRequestHandler;
    }

    @Override
    public void getUserInformation(UserInformationRequest request, StreamObserver<UserInformation> responseObserver) {
        var userInformation = userRequestHandler.getUserInformation(request);
        responseObserver.onNext(userInformation);
        responseObserver.onCompleted();
    }


    @Override
    public void tradeStock(StockTradeRequest request, StreamObserver<StockTradeResponse> responseObserver) {
        super.tradeStock(request, responseObserver);
    }
}
