package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.cryptimeleon.incentive.services.basket.model.RewardItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketTest {

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
        log.info("Creating new basket");
        var createResponse = createBasket(webClient);

        log.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        log.info("Querying basket");
        var basketResponse = queryBasket(webClient, basketId);

        var basket = basketResponse.getResponseBody();
        log.info("Basket response: " + basket);

        assert basket != null;
        assertThat(basket.getItems()).isEmpty();
        assertThat(basket.isPaid()).isFalse();
        assertThat(basket.isRedeemed()).isFalse();
        assertThat(basket.getBasketID()).isEqualByComparingTo(basketId);

        log.info("Delete Basket");
        deleteBasket(webClient, basketId);

        log.info("Query deleted basket");
        queryBasket(webClient, basketId, HttpStatus.NOT_FOUND);
    }

    @Test
    void basketItemsTest(@Autowired WebTestClient webTestClient) {
        log.info("Creating new basket");
        var createResponse = createBasket(webTestClient);

        log.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        log.info("Querying all items");
        var itemsResponse = getItems(webTestClient);
        log.info("All items: " + itemsResponse);

        var items = itemsResponse.getResponseBody();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        log.info("Query existing item");
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
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build("12341234123"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);

        log.info("Try adding invalid items");
        putItem(webTestClient, basketId, firstTestItem.getId(), -2, HttpStatus.UNPROCESSABLE_ENTITY);
        putItem(webTestClient, basketId, "1234123412", 2, HttpStatus.NOT_FOUND);
        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).isEmpty();

        log.info("Add some item");
        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).containsEntry(firstTestItem.getId(), 5);

        log.info("Add and update items");
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice() + secondTestItem.getPrice());

        log.info("Delete item");
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .doesNotContainKey(secondTestItem.getId());
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice());

        log.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }

    /**
     * Testing rewards that only can be added by clients that know the redeemSecret.
     */
    @Test
    void rewardItemTest(@Autowired WebTestClient webTestClient) {
        log.info("Creating new basket");
        var createResponse = createBasket(webTestClient);

        log.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        log.info("Querying all items");
        var rewardsResponse = getRewards(webTestClient);
        log.info("All reward items: " + rewardsResponse);

        var rewards = rewardsResponse.getResponseBody();

        log.info("Query basket before, assert no rewards present");
        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        log.info("basket: " + basket);
        assertThat(basket.getRewardItems()).hasSize(0);

        log.info("Add rewards without valid secret needed for authentication");
        postRewards(webTestClient, "wrong-secret", basketId, Arrays.stream(rewards).limit(2).map(RewardItem::toString).collect(Collectors.toList()), HttpStatus.UNAUTHORIZED);

        log.info("Add rewards with valid secret");
        var rewardsToAdd = Arrays.stream(rewards).limit(2).map(RewardItem::toString).collect(Collectors.toList());
        postRewards(webTestClient, redeemSecret, basketId, rewardsToAdd, HttpStatus.OK);

        log.info("Query basket, check that there are indeed two rewards");
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        log.info("basket: " + basket);
        assertThat(basket.getRewardItems()).containsExactly(rewardsToAdd.toArray(String[]::new));
    }
}
