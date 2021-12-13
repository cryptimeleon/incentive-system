package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClientException;
import org.cryptimeleon.incentive.client.dto.PostRedeemBasketDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Slf4j
public class BasketTest extends IncentiveSystemIntegrationTest {

    @Value("${basket-service.pay-secret}")
    private String paymentSecret;

    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    @Test
    void testGetBasket() {
        var basketClient = new BasketClient(basketUrl);

        log.info("Testing getBasket for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> basketClient.getBasket(wrongBasketId).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        log.info("Testing getBasket for existing basket");
        var basketId = basketClient.createBasket().block();

        var basket = basketClient.getBasket(basketId).block();
        assertThat(basket).isNotNull().satisfies(b -> b.getBasketID().equals(basketId));
    }

    @Test
    void testRedeemBasket() {
        var basketClient = new BasketClient(basketUrl);

        log.info("Create new basket and adding items");
        UUID basketId = basketClient.createBasket().block();
        var items = basketClient.getItems().block();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        basketClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();

        var basket = basketClient.getBasket(basketId).block();

        log.info("Redeeming not paid basket throws exception");
        var redeemRequest = new PostRedeemBasketDto(basketId, "Some request", basket.getValue());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                basketClient.redeemBasket(redeemRequest, redeemSecret).block())
                .withCauseInstanceOf(IncentiveClientException.class);


        basketClient.payBasket(basketId, basket.getValue(), paymentSecret).block();

        log.info("Payed basket can be redeemed");
        basketClient.redeemBasket(redeemRequest, redeemSecret).block();
        basket = basketClient.getBasket(basketId).block();
        assertThat(basket.isRedeemed()).isTrue();
    }
}
