package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.model.Basket;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.cryptimeleon.incentive.services.basket.model.RewardItem;
import org.cryptimeleon.incentive.services.basket.model.requests.PutItemRequest;
import org.cryptimeleon.incentive.services.basket.model.requests.RedeemBasketRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

public class ClientHelper {
    public static EntityExchangeResult<UUID> createBasket(WebTestClient webClient) {
        return webClient.get()
                .uri("/basket/new")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UUID.class)
                .returnResult();
    }

    public static EntityExchangeResult<Basket> queryBasket(WebTestClient webClient, UUID basketId) {
        return queryBasket(webClient, basketId, HttpStatus.OK);
    }

    static EntityExchangeResult<Basket> queryBasket(WebTestClient webClient, UUID basketId, HttpStatus expectedStatus) {
        return webClient.get()
                .uri("/basket")
                .header("basketId", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(Basket.class)
                .returnResult();
    }

    static void deleteBasket(WebTestClient webClient, UUID basketId) {
        webClient.delete()
                .uri("/basket")
                .header("basketId", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    public static void newItem(WebTestClient webTestClient, Item item, String providerSecret, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/items")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(item))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static void deleteAllItems(WebTestClient webTestClient, String providerSecret, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri("/items")
                .header("provider-secret", providerSecret)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static EntityExchangeResult<Item[]> getItems(WebTestClient webTestClient) {
        return webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Item[].class)
                .returnResult();
    }

    public static void newRewardItem(WebTestClient webTestClient, RewardItem rewardItem, String providerSecret, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/reward-items")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(rewardItem))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static EntityExchangeResult<RewardItem[]> getRewards(WebTestClient webTestClient) {
        return webTestClient.get()
                .uri("/reward-items")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RewardItem[].class)
                .returnResult();
    }

    public static void deleteAllRewardItems(WebTestClient webTestClient, String providerSecret, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri("/reward-items")
                .header("provider-secret", providerSecret)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static void putItem(WebTestClient webTestClient, UUID basketId, String itemId, int count, HttpStatus expectedStatus) {
        var putRequest = new PutItemRequest(basketId, itemId, count);
        putItem(webTestClient, putRequest, expectedStatus);
    }

    static void putItem(WebTestClient webTestClient, PutItemRequest putItemRequest, HttpStatus expectedStatus) {
        webTestClient.put()
                .uri("/basket/items")
                .body(BodyInserters.fromValue(putItemRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void postRewards(WebTestClient webTestClient, String redeemSecret, UUID basketId, List<String> rewardIds, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/basket/rewards")
                .header("redeem-secret", redeemSecret)
                .header("basket-id", basketId.toString())
                .body(BodyInserters.fromValue(rewardIds))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void deleteBasketItem(WebTestClient webTestClient, UUID basketId, String itemId, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/basket/items")
                        .queryParam("itemId", itemId)
                        .build())
                .header("basketId", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static void payBasket(WebTestClient webTestClient, UUID basketId, HttpStatus expectedStatus, String paySecret) {
        webTestClient.post()
                .uri("/basket/pay")
                .header("pay-secret", paySecret)
                .header("basket-id", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void redeemBasket(WebTestClient webTestClient, RedeemBasketRequest redeemRequest, HttpStatus expectedStatus, String redeemSecret) {
        webTestClient.post()
                .uri("/basket/redeem")
                .header("redeem-secret", redeemSecret)
                .body(BodyInserters.fromValue(redeemRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void redeemBasket(WebTestClient webTestClient, UUID basketId, String request, long value, HttpStatus expectedStatus, String redeemSecret) {
        var redeemRequest = new RedeemBasketRequest(basketId, request, value);
        redeemBasket(webTestClient, redeemRequest, expectedStatus, redeemSecret);
    }

}
