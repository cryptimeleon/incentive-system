package de.upb.crypto.incentive.client.integrationtest;

import de.upb.crypto.incentive.client.BasketserverClient;
import de.upb.crypto.incentive.client.dto.PostRedeemBasketDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        var webClient = WebClient.builder().baseUrl(basketserverUrl).build();
        var basketServerClient = new BasketserverClient(webClient);

        logger.info("Testing getBasket for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        // TODO improve exception handling of webclient
        assertThatExceptionOfType(WebClientResponseException.class).isThrownBy(() -> basketServerClient.getBasket(wrongBasketId).block());

        logger.info("Testing getBasket for existing basket");
        var basketId = basketServerClient.createBasket().block();

        var basket = basketServerClient.getBasket(basketId).block();
        assertThat(basket).isNotNull().satisfies(b -> b.getBasketID().equals(basketId));
    }

    @Test
    void testRedeemBasket() {
        var webClient = WebClient.builder().baseUrl(basketserverUrl).build();
        var basketServerClient = new BasketserverClient(webClient);

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
        // TODO improve exception handling of webclient
        assertThatExceptionOfType(WebClientResponseException.class).isThrownBy(() ->
                basketServerClient.redeemBasket(redeemRequest, redeemSecret).block());


        basketServerClient.payBasket(basketId, basket.getValue(), paymentSecret).block();

        logger.info("Payed basket can be redeemed");
        basket = basketServerClient.getBasket(basketId).block();
        basketServerClient.redeemBasket(redeemRequest, redeemSecret).block();
        basket = basketServerClient.getBasket(basketId).block();
        Assertions.assertThat(basket.isRedeemed()).isTrue();
    }
}
