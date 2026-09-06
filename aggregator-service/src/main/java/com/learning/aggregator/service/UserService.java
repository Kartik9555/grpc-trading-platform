package com.learning.aggregator.service;

import com.leaning.user.UserInformation;
import com.leaning.user.UserInformationRequest;
import com.leaning.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userClient;

    public UserInformation getUserInformation(Integer userId) {
        var request = UserInformationRequest.newBuilder()
                .setUserId(userId)
                .build();

        return this.userClient.getUserInformation(request);
    }
}
