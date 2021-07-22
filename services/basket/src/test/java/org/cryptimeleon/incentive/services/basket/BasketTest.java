package org.cryptimeleon.incentive.services.basket;

import org.assertj.core.api.Assertions;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketTest {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Value("${basket-service.pay-secret}")
    private String paymentSecret;

    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    @Test
    void helloWorldTest(@Autowired WebTestClient webClient) {
        Assertions.assertThat(
                webClient.get()
                        .uri("/")
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(String.class)
                        .returnResult().getResponseBody()).contains("Basket");
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

        assert basket != null;
        assertThat(basket.getItems()).isEmpty();
        assertThat(basket.isPaid()).isFalse();
        assertThat(basket.isRedeemed()).isFalse();
        assertThat(basket.getBasketID()).isEqualByComparingTo(basketId);

        logger.info("Delete Basket");
        deleteBasket(webClient, basketId);

        logger.info("Query deleted basket");
        queryBasket(webClient, basketId, HttpStatus.NOT_FOUND);
    }

    @Test
    void basketItemsTest(@Autowired WebTestClient webTestClient) {
        logger.info("Creating new basket");
        var createResponse = createBasket(webTestClient);

        logger.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Querying all items");
        var itemsResponse = getItems(webTestClient);
        logger.info("All items: " + itemsResponse);

        var items = itemsResponse.getResponseBody();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        logger.info("Query existing item");
        var firstItemOtherUri = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build(firstTestItem.getId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Item.class)
                .returnResult()
                .getResponseBody();
        assertThat(firstItemOtherUri).isEqualTo(firstTestItem);

        // Check that correct error handling is used
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build(UUID.randomUUID()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);

        logger.info("Try adding invalid items");
        putItem(webTestClient, basketId, firstTestItem.getId(), -2, HttpStatus.UNPROCESSABLE_ENTITY);
        putItem(webTestClient, basketId, UUID.randomUUID(), 2, HttpStatus.NOT_FOUND);
        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).isEmpty();

        logger.info("Add some item");
        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).containsEntry(firstTestItem.getId(), 5);

        logger.info("Add and update items");
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice() + secondTestItem.getPrice());

        logger.info("Delete item");
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .doesNotContainKey(secondTestItem.getId());
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice());

        logger.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }
}
