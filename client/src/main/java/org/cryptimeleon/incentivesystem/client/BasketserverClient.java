package org.cryptimeleon.incentivesystem.client;

import org.cryptimeleon.incentivesystem.client.dto.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;


public class BasketserverClient {

    /*
     * Webclient configured with the url of the basketserver
     */
    private WebClient basketClient;

    public BasketserverClient(String basketserverUrl) {
        this.basketClient = WebClientHelper.buildWebClient(basketserverUrl);
    }

    /*
     * Sends a request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return basketClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<BasketDto> getBasket(UUID basketId) {
        return basketClient.get()
                .uri("/basket")
                .header("basketId", String.valueOf(basketId))
                .retrieve()
                .bodyToMono(BasketDto.class);
    }

    public Mono<UUID> createBasket() {
        return basketClient.get()
                .uri("/basket/new")
                .retrieve()
                .bodyToMono(UUID.class);
    }

    public Mono<BasketItemDto[]> getItems() {
        return basketClient.get()
                .uri("/items")
                .retrieve()
                .bodyToMono(BasketItemDto[].class);
    }

    public Mono<Void> putItemToBasket(UUID basketId, UUID itemId, int count) {
        var putItemDto = new PutItemDto(basketId, itemId, count);
        return putItemToBasket(putItemDto);
    }

    public Mono<Void> putItemToBasket(PutItemDto putItemDto) {
        return basketClient.put()
                .uri("/basket/items")
                .body(BodyInserters.fromValue(putItemDto))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> payBasket(UUID basketId, long value, String paymentSecret) {
        var postPayBasketDto = new PostPayBasketDto(basketId, value);
        return payBasket(postPayBasketDto, paymentSecret);
    }

    public Mono<Void> payBasket(PostPayBasketDto postPayBasketDto, String paymentSecret) {
        return basketClient.post()
                .uri("/basket/pay")
                .header("pay-secret", paymentSecret)
                .body(BodyInserters.fromValue(postPayBasketDto))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> redeemBasket(PostRedeemBasketDto postRedeemBasketDto, String redeemSecret) {
        return basketClient.post()
                .uri("/basket/redeem")
                .header("redeem-secret", redeemSecret)
                .body(BodyInserters.fromValue(postRedeemBasketDto))
                .retrieve()
                .bodyToMono(Void.class);
    }
}
