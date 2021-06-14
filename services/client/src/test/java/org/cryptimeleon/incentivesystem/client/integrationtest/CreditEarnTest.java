package org.cryptimeleon.incentivesystem.client.integrationtest;

import org.cryptimeleon.incentivesystem.client.BasketClient;
import org.cryptimeleon.incentivesystem.client.CreditClient;
import org.cryptimeleon.incentivesystem.client.IncentiveClientException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CreditEarnTest extends IncentiveSystemIntegrationTest {

    Logger logger = LoggerFactory.getLogger(BasketTest.class);

    @Test
    void testBasketConnection() {
        var creditClient = new CreditClient(creditUrl);

        var basketClient = new BasketClient(basketUrl);

        logger.info("Test earn without valid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", UUID.randomUUID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        logger.info("Test earn with unpaid basket should fail");
        var basket = TestHelper.createBasketWithItems(basketUrl);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(UUID.randomUUID(), "Some request", basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        logger.info("Test earn with paid basket should succeed");
        basketClient.payBasket(basket.getBasketID(), basket.getValue(), paySecret).block();
        var mybasket = basketClient.getBasket(basket.getBasketID()).block();
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
