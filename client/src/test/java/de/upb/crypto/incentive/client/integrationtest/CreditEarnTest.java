package de.upb.crypto.incentive.client.integrationtest;

import de.upb.crypto.incentive.client.BasketserverClient;
import de.upb.crypto.incentive.client.CreditClient;
import de.upb.crypto.incentive.client.IncentiveClientException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CreditEarnTest extends IncentiveSystemIntegrationTest {

    Logger logger = LoggerFactory.getLogger(BasketServerTest.class);

    @Test
    void testBasketServerConnection() {
        var creditClient = new CreditClient(creditUrl);

        var basketserverClient = new BasketserverClient(basketserverUrl);

        logger.info("Test earn without valid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", UUID.randomUUID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        logger.info("Test earn with unpaid basket should fail");
        var basket = TestHelper.createBasketWithItems(basketserverUrl);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        logger.info("Test earn with paid basket should succeed");
        basketserverClient.payBasket(basket.getBasketID(), basket.getValue(), paySecret).block();
        var mybasket = basketserverClient.getBasket(basket.getBasketID()).block();
        var earnWithPaidBasket = creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", basket.getBasketID()).block();

        logger.info("Second earn with paid basket and same request should succeed");
        var secondEarnWithPaidBasket = creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", basket.getBasketID()).block();
        assertThat(earnWithPaidBasket.getSerializedCreditResponse()).isEqualTo(secondEarnWithPaidBasket.getSerializedCreditResponse());


        logger.info("Second earn with paid basket and other request should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(UUID.randomUUID(), "Some other request", basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);
    }
}
