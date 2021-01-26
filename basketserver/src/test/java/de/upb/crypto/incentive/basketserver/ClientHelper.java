package de.upb.crypto.incentive.basketserver;

import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.Item;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.UUID;

public class ClientHelper {
    static EntityExchangeResult<UUID> createBasket(WebTestClient webClient) {
        return webClient.get()
                .uri("/basket/new")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UUID.class)
                .returnResult();
    }

    static EntityExchangeResult<Basket> queryBasket(WebTestClient webClient, UUID basketId) {
        return queryBasket(webClient, basketId, HttpStatus.OK);
    }

    static EntityExchangeResult<Basket> queryBasket(WebTestClient webClient, UUID basketId, HttpStatus expectedStatus) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/basket")
                        .queryParam("basketId", basketId)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(Basket.class)
                .returnResult();
    }

    static void deleteBasket(WebTestClient webClient, UUID basketId) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/basket")
                        .queryParam("basketId", basketId)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    static EntityExchangeResult<Item[]> getItems(WebTestClient webTestClient) {
        return webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Item[].class)
                .returnResult();
    }

    static void putItem(WebTestClient webTestClient, UUID basketId, UUID itemId, int count, HttpStatus expectedStatus) {
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/basket/items")
                        .queryParam("basketId", basketId)
                        .queryParam("itemId", itemId)
                        .queryParam("count", count).build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void deleteBasketItem(WebTestClient webTestClient, UUID basketId, UUID itemId, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/basket/items")
                        .queryParam("basketId", basketId)
                        .queryParam("itemId", itemId)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void payBasket(WebTestClient webTestClient, UUID basketId, int value, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/basket/pay")
                        .queryParam("basketId", basketId)
                        .queryParam("value", value)
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void redeemBasket(WebTestClient webTestClient, RedeemRequest redeemRequest, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/basket/redeem")
                .body(BodyInserters.fromValue(redeemRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static void redeemBasket(WebTestClient webTestClient, UUID basketId, String request, int value, HttpStatus expectedStatus) {
        var redeemRequest = new RedeemRequest(basketId, request, value);
        redeemBasket(webTestClient, redeemRequest, expectedStatus);
    }
}
