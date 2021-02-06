package de.upb.crypto.incentive.basketserver;

import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.Item;
import de.upb.crypto.incentive.basketserver.model.requests.PayBasketRequest;
import de.upb.crypto.incentive.basketserver.model.requests.PutItemRequest;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemBasketRequest;
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

    static void deleteBasket(WebTestClient webClient, UUID basketId) {
        webClient.delete()
                .uri("/basket")
                .header("basketId", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT);
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

    public static void putItem(WebTestClient webTestClient, UUID basketId, UUID itemId, int count, HttpStatus expectedStatus) {
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

    static void deleteBasketItem(WebTestClient webTestClient, UUID basketId, UUID itemId, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/basket/items")
                        .queryParam("itemId", itemId)
                        .build())
                .header("basketId", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    public static void payBasket(WebTestClient webTestClient, UUID basketId, long value, HttpStatus expectedStatus, String paySecret) {
        var payRequest = new PayBasketRequest(basketId, value);
        payBasket(webTestClient, payRequest, expectedStatus, paySecret);
    }

    static void payBasket(WebTestClient webTestClient, PayBasketRequest payBasketRequest, HttpStatus expectedStatus, String paySecret) {
        webTestClient.post()
                .uri("/basket/pay")
                .header("pay-secret", paySecret)
                .body(BodyInserters.fromValue(payBasketRequest))
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
