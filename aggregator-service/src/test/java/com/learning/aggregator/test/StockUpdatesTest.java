package com.learning.aggregator.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leaning.common.Ticker;
import com.leaning.stock.PriceUpdate;
import com.learning.aggregator.dto.PriceUpdateDto;
import com.learning.aggregator.test.mockservice.StockMockService;
import com.learning.aggregator.test.mockservice.UserMockService;
import net.devh.boot.grpc.server.service.GrpcService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@DirtiesContext
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.stock-service.address=in-process:integration-test"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockUpdatesTest {
    private static final Logger log = LoggerFactory.getLogger(StockUpdatesTest.class);
    private static final String STOCK_UPDATES_ENDPOINT = "http://localhost:%d/stock/updates";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void stockUpdatesTest() {
        var list = this.restTemplate.execute(
                STOCK_UPDATES_ENDPOINT.formatted(port), HttpMethod.GET, null, this::getResponse);
        Assertions.assertEquals(5, list.size());
        Assertions.assertEquals(Ticker.AMAZON.toString(), list.getFirst().ticker());
        Assertions.assertEquals(1, list.getFirst().price());
    }

    private List<PriceUpdateDto> getResponse(ClientHttpResponse response) throws IOException {
        var list = new ArrayList<PriceUpdateDto>();
        try (var reader = new BufferedReader(new InputStreamReader(response.getBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.isEmpty()) {
                    log.info(line);
                    var dto = objectMapper.readValue(line.substring("data:".length()), PriceUpdateDto.class);
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("streaming error", e);
        }
        return list;
    }

    @TestConfiguration
    static class TestConfig {
        @GrpcService
        public StockMockService stockMockService() {
            return new StockMockService();
        }
    }
}
