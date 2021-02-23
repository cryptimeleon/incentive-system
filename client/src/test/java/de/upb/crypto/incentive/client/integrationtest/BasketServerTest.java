package org.cryptimeleon.incentivesystem.client.integrationtest;

import org.cryptimeleon.incentivesystem.client.BasketserverClient;
import org.cryptimeleon.incentivesystem.client.IncentiveClientException;
import org.cryptimeleon.incentivesystem.client.dto.PostRedeemBasketDto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BasketServerTest extends IncentiveSystemIntegrationTest {

    Logger logger = LoggerFactory.getLogger(BasketServerTest.class);

    @Value("${basketserver.pay-secret}")
    private String paymentSecret;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    @Test
    void testGetBasket() {
        var basketServerClient = new BasketserverClient(basketserverUrl);

        logger.info("Testing getBasket for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> basketServerClient.getBasket(wrongBasketId).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        logger.info("Testing getBasket for existing basket");
        var basketId = basketServerClient.createBasket().block();

        var basket = basketServerClient.getBasket(basketId).block();
        assertThat(basket).isNotNull().satisfies(b -> b.getBasketID().equals(basketId));
    }

    @Test
    void testRedeemBasket() {
        var basketServerClient = new BasketserverClient(basketserverUrl);

        logger.info("Create new basket and adding items");
        UUID basketId = basketServerClient.createBasket().block();
        var items = basketServerClient.getItems().block();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        basketServerClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketServerClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();

        var basket = basketServerClient.getBasket(basketId).block();

        logger.info("Redeeming not paid basket throws exception");
        var redeemRequest = new PostRedeemBasketDto(basketId, "Some request", basket.getValue());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                basketServerClient.redeemBasket(redeemRequest, redeemSecret).block())
                .withCauseInstanceOf(IncentiveClientException.class);


        basketServerClient.payBasket(basketId, basket.getValue(), paymentSecret).block();

        logger.info("Payed basket can be redeemed");
        basketServerClient.redeemBasket(redeemRequest, redeemSecret).block();
        basket = basketServerClient.getBasket(basketId).block();
        assertThat(basket.isRedeemed()).isTrue();
    }
}
