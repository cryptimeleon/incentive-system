package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BasketTest extends TransactionTestPreparation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasketTest.class);

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
}
