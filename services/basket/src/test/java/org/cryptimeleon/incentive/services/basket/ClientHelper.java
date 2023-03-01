package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

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

    public static EntityExchangeResult<Basket> queryBasketUrlParam(WebTestClient webClient, UUID basketId, HttpStatus expectedStatus) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/basket").queryParam("basketId", String.valueOf(basketId)).build())
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

    public static void payBasket(WebTestClient webTestClient, UUID basketId, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/basket/pay-dev")
                .header("basket-id", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }
}
