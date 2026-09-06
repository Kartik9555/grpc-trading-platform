package com.learning.user.test;

import com.leaning.common.Ticker;
import com.leaning.user.StockTradeRequest;
import com.leaning.user.TradeAction;
import com.leaning.user.UserInformationRequest;
import com.leaning.user.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.user-service.address=in-process:integration-test"
})
public class UserServiceTest {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub stub;

    @Test
    public void userInformationTest(){
        var request = UserInformationRequest.newBuilder()
                .setUserId(1)
                .build();
        var response = stub.getUserInformation(request);
        Assertions.assertEquals(10_000, response.getBalance());
        Assertions.assertEquals("Sam", response.getName());
        Assertions.assertTrue(response.getHoldingsList().isEmpty());
    }

    @Test
    public void unknownUserTest() {
        var exception = Assertions.assertThrows(StatusRuntimeException.class, () -> {
                var request = UserInformationRequest.newBuilder()
                .setUserId(10)
                .build();
            stub.getUserInformation(request);
        });
        Assertions.assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    public void unknowTicketBuyTest() {
        var exception = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setAction(TradeAction.BUY)
                    .setPrice(1)
                    .setQuantity(1)
                    .build();
            stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void insufficientSharesTest() {
        var exception = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setTicker(Ticker.AMAZON)
                    .setAction(TradeAction.SELL)
                    .setPrice(1)
                    .setQuantity(1000)
                    .build();
            stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.FAILED_PRECONDITION, exception.getStatus().getCode());
    }

    @Test
    public void insufficientBalanceTest() {
        var exception = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setTicker(Ticker.AMAZON)
                    .setAction(TradeAction.BUY)
                    .setPrice(1)
                    .setQuantity(10001)
                    .build();
            stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.FAILED_PRECONDITION, exception.getStatus().getCode());
    }

    @Test
    public void buySellTest(){
        // buy
        var buyRequest = StockTradeRequest.newBuilder()
                .setUserId(2)
                .setTicker(Ticker.AMAZON)
                .setAction(TradeAction.BUY)
                .setPrice(100)
                .setQuantity(5)
                .build();
        var buyResponse = stub.tradeStock(buyRequest);

        // validate balance
        Assertions.assertEquals(9500, buyResponse.getBalance());

        // check holding
        var userRequest = UserInformationRequest.newBuilder()
                .setUserId(2)
                .build();
        var userResponse = stub.getUserInformation(userRequest);
        Assertions.assertFalse(userResponse.getHoldingsList().isEmpty());
        Assertions.assertEquals(1, userResponse.getHoldingsCount());
        Assertions.assertEquals(Ticker.AMAZON, userResponse.getHoldingsList().getFirst().getTicker());

        // sell
        var sellRequest = buyRequest.toBuilder()
                .setAction(TradeAction.SELL)
                .setPrice(102)
                .build();
        var sellResponse = stub.tradeStock(sellRequest);

        // validate balance
        Assertions.assertEquals(10010, sellResponse.getBalance());
    }
}
