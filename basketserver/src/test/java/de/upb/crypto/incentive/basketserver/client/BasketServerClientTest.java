package de.upb.crypto.incentive.basketserver.client;

import de.upb.crypto.incentive.basketserver.BasketController;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemBasketRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

import static de.upb.crypto.incentive.basketserver.ClientHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketServerClientTest {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @LocalServerPort
    private int port;

    @Value("${basketserver.pay-secret}")
    private String paymentSecret;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    @Test
    void testGetBasket(@Autowired WebTestClient webTestClient) {
        var baseUrl = String.format("http://localhost:%s", port);
        var webClient = WebClient.builder().baseUrl(baseUrl).build();
        var basketServerClient = new BasketServerClient(webClient, paymentSecret, redeemSecret);

        logger.info("Testing getBasket for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        // TODO improve exception handling of webclient
        assertThatExceptionOfType(WebClientResponseException.class).isThrownBy(() -> basketServerClient.getBasket(wrongBasketId).block());

        logger.info("Testing getBasket for existing basket");
        var basketId = createBasket(webTestClient).getResponseBody();

        var basket = basketServerClient.getBasket(basketId).block();
        assertThat(basket).isNotNull().satisfies(b -> b.getBasketID().equals(basketId));
    }

    @Test
    void testRedeemBasket(@Autowired WebTestClient webTestClient) {
        var baseUrl = String.format("http://localhost:%s", port);
        var webClient = WebClient.builder().baseUrl(baseUrl).build();
        var basketServerClient = new BasketServerClient(webClient, paymentSecret, redeemSecret);

        logger.info("Create new basket and adding items");
        UUID basketId = createBasket(webTestClient).getResponseBody();
        var items = getItems(webTestClient).getResponseBody();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();

        logger.info("Redeeming not paid basket throws exception");
        var redeemRequest = new RedeemBasketRequest(basketId, "Some request", basket.getValue());
        // TODO improve exception handling of webclient
        assertThatExceptionOfType(WebClientResponseException.class).isThrownBy(() ->
                basketServerClient.redeem(redeemRequest).block());


        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK, paymentSecret);

        logger.info("Payed basket can be redeemed");
        basketServerClient.redeem(redeemRequest).block();
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isRedeemed()).isTrue();
    }
}
