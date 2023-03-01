package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.PostRedeemBasketDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BasketTest extends TransactionTestPreparation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasketTest.class);
    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    @BeforeAll
    void setup() {
        prepareBasketServiceAndPromotions();
    }

    @Test
    void testGetBasket() {
        var basketClient = new BasketClient(basketUrl);
        log.info("Testing getBasket for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> basketClient.getBasket(wrongBasketId).block());
        log.info("Testing getBasket for existing basket");
        var basketId = basketClient.createBasket().block();
        var basket = basketClient.getBasket(basketId).block();
        assertThat(basket).isNotNull();
        assertThat(basket.getBasketID()).isEqualTo(basketId);
    }

    @Test
    void testRedeemBasket() {
        var basketClient = new BasketClient(basketUrl);
        log.info("Create new basket and adding items");
        UUID basketId = basketClient.createBasket().block();
        basketClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();
        var basket = basketClient.getBasket(basketId).block();
        log.info("Redeeming not paid basket throws exception");
        var redeemRequest = new PostRedeemBasketDto(basketId, "Some request", basket.getValue());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> basketClient.redeemBasket(redeemRequest, redeemSecret).block());
        basketClient.payBasket(basketId);
        log.info("Paid basket can be redeemed");
        basketClient.redeemBasket(redeemRequest, redeemSecret).block();
        basket = basketClient.getBasket(basketId).block();
        assertThat(basket).isNotNull();
        assertThat(basket.isRedeemed()).isTrue();
    }
}
