package de.upb.crypto.incentive.client.integrationtest;

import de.upb.crypto.incentive.client.BasketserverClient;
import de.upb.crypto.incentive.client.CreditClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;


public class CreditEarnTest extends IncentiveSystemIntegrationTest {

    @Test
    void testBasketServerConnection() {
        var creditWebClient = WebClient.builder().baseUrl(creditUrl).build();
        var creditClient = new CreditClient(creditWebClient);

        var basketserverWebClient = WebClient.builder().baseUrl(basketserverUrl).build();
        var basketserverClient = new BasketserverClient(basketserverWebClient);

        var creditResponseWithoutBasket = creditClient.sendEarnRequest();
    }
}
