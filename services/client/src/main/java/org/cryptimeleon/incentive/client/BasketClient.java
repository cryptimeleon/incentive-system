package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client calls for info service.
 * Can be used for testing and prototyping.
 */
public class BasketClient implements AliveEndpoint {

    /**
     * Webclient configured with the url of the basket service
     */
    private WebClient basketClient;

    public BasketClient(String basketUrl) {
        this.basketClient = WebClientHelper.buildWebClient(basketUrl);
    }

    /**
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

    public Mono<ItemDto[]> getItems() {
        return basketClient.get()
                .uri("/items")
                .retrieve()
                .bodyToMono(ItemDto[].class);
    }

    public Mono<Void> newBasketItem(ItemDto item, String providerSecret) {
        return basketClient.post()
                .uri("/items")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(item))
                .retrieve()
                .bodyToMono(Void.class);
    }


    public Mono<Void> newRewardItem(RewardItemDto rewardItem, String providerSecret) {
        return basketClient.post()
                .uri("/reward-items")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(rewardItem))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> putItemToBasket(UUID basketId, String itemId, int count) {
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
        return payBasket(basketId, paymentSecret);
    }

    public Mono<Void> payBasket(UUID basketId, String paymentSecret) {
        return basketClient.post()
                .uri("/basket/pay")
                .header("pay-secret", paymentSecret)
                .header("basket-id", String.valueOf(basketId))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Deprecated
    public Mono<Void> payBasket(PostPayBasketDto postPayBasketDto, String paymentSecret) {
        return payBasket(postPayBasketDto.getBasketId(), paymentSecret);
    }

    public Mono<Void> redeemBasket(PostRedeemBasketDto postRedeemBasketDto, String redeemSecret) {
        return basketClient.post()
                .uri("/basket/redeem")
                .header("redeem-secret", redeemSecret)
                .body(BodyInserters.fromValue(postRedeemBasketDto))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> lockBasket(UUID basketId, String redeemSecret) {
        return basketClient.post()
                .uri("/basket/lock")
                .header("redeem-secret", redeemSecret)
                .body(BodyInserters.fromValue(basketId))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> setRewardsForBasket(UUID basketId, ArrayList<String> rewardIds, String redeemSecret) {
        return basketClient.post()
                .uri("/basket/rewards")
                .header("redeem-secret", redeemSecret)
                .header("basket-id", String.valueOf(basketId))
                .body(BodyInserters.fromValue(rewardIds))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public void addShoppingItems(List<ItemDto> testBasketItems, String providerSecret) {
        testBasketItems.forEach(item ->
                basketClient.post()
                        .uri("/items")
                        .header("provider-secret", providerSecret)
                        .body(BodyInserters.fromValue(item))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block()
        );
    }
}
