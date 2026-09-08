package com.learning.aggregator.test;

import com.leaning.common.Ticker;
import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.leaning.user.TradeAction;
import com.leaning.user.UserInformation;
import com.learning.aggregator.test.mockservice.StockMockService;
import com.learning.aggregator.test.mockservice.UserMockService;
import net.devh.boot.grpc.server.service.GrpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.user-service.address=in-process:integration-test",
        "grpc.client.stock-service.address=in-process:integration-test"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTradeTest {

    private static final String USER_INFORMATION_ENDPOINT = "http://localhost:%d/user/%d";
    private static final String TRADE_ENDPOINT = "http://localhost:%d/trade";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void userInformationTest(){
        var endpoint = USER_INFORMATION_ENDPOINT.formatted(port, 1);
        var response = this.restTemplate.getForEntity(endpoint, UserInformation.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        var userInformation = response.getBody();
        Assertions.assertNotNull(userInformation);
        Assertions.assertEquals(1, userInformation.getUserId());
        Assertions.assertEquals("integration-test", userInformation.getName());
        Assertions.assertEquals(100, userInformation.getBalance());
    }

    @Test
    public void unknownUserInformationTest(){
        var endpoint = USER_INFORMATION_ENDPOINT.formatted(port, 2);
        var response = this.restTemplate.getForEntity(endpoint, UserInformation.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        var userInformation = response.getBody();
        Assertions.assertNull(userInformation);
    }

    @Test
    public void tradeTest(){
        var endpoint = TRADE_ENDPOINT.formatted(port);
        var tradeRequest = StockTradeRequest.newBuilder()
                .setUserId(1)
                .setPrice(10)
                .setTicker(Ticker.AMAZON)
                .setAction(TradeAction.BUY)
                .setQuantity(2)
                .build();
        var response = this.restTemplate.postForEntity(endpoint, tradeRequest, StockTradeResponse.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        var stockTradeResponse = response.getBody();
        Assertions.assertNotNull(stockTradeResponse);
        Assertions.assertEquals(1, stockTradeResponse.getUserId());
        Assertions.assertEquals(Ticker.AMAZON, stockTradeResponse.getTicker());
        Assertions.assertEquals(TradeAction.BUY, stockTradeResponse.getAction());
        Assertions.assertEquals(2, stockTradeResponse.getQuantity());
        Assertions.assertEquals(15, stockTradeResponse.getPrice());
        Assertions.assertEquals(1000, stockTradeResponse.getTotalPrice());
        Assertions.assertEquals(0, stockTradeResponse.getBalance());
    }

    @TestConfiguration
    static class TestConfig {
        @GrpcService
        public StockMockService stockMockService() {
            return new StockMockService();
        }

        @GrpcService
        public UserMockService userMockService() {
            return new UserMockService();
        }
    }
}
