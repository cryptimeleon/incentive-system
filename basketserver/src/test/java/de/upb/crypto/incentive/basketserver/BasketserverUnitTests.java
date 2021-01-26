package de.upb.crypto.incentive.basketserver;

import de.upb.crypto.incentive.basketserver.model.Basket;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketserverUnitTests {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    // Create basket
    // Delete Basket
    // Query Basket
    // Query invalid Basket
    // Add item
    // Add invalid item
    // Update item
    // Remove item


    @Test
    void helloWorldTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Hello World");
    }

    private EntityExchangeResult<UUID> createBasket(WebTestClient webClient) {
        return webClient.get()
                .uri("/basket/new")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UUID.class)
                .returnResult();
    }

    private EntityExchangeResult<Basket> queryBasket(WebTestClient webClient, UUID basketId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/basket")
                        .queryParam("basketId", basketId)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Basket.class)
                .returnResult();
    }

    private void deleteBasket(WebTestClient webClient, UUID basketId) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/basket")
                        .queryParam("basketId", basketId)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createDeleteBasketTest(@Autowired WebTestClient webClient) {
        logger.info("Creating new basket");
        var createResponse = createBasket(webClient);

        logger.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Querying basket");
        var basketResponse = queryBasket(webClient, basketId);

        var basket = basketResponse.getResponseBody();
        logger.info("Basket response: " + basket);

        assertThat(basket.getItems()).isEmpty();
        assertThat(basket.isPaid()).isFalse();
        assertThat(basket.isRedeemed()).isFalse();
        assertThat(basket.getBasketID()).isEqualByComparingTo(basketId);

        logger.info("Delete Basket");
        deleteBasket(webClient, basketId);

        logger.info("Query deleted basket");
        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/basket")
                        .queryParam("basketId", basketId)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

}
