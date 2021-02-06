package de.upb.crypto.incentive.client.integrationtest;

import de.upb.crypto.incentive.client.CreditClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

public class IncentiveSystemIntegrationTest implements IntegrationTest {

    @Test
    void testHelloWorld() {
        var creditWebClient = WebClient.builder().baseUrl("http://localhost:8002").build();
        var creditClient = new CreditClient(creditWebClient, "", "");
        var result = creditClient.sendHelloWorldRequest().block();
        System.out.println("Result: " + result);
        System.out.println("Finish test");
    }
}
