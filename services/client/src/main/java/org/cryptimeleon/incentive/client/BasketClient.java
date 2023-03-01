package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.*;
import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.cryptimeleon.incentive.client.dto.store.EarnRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.SpendRequestStoreDto;
import org.cryptimeleon.incentive.crypto.model.EarnStoreRequest;
import org.cryptimeleon.incentive.crypto.model.SpendCouponRequest;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client calls for info service.
 * Can be used for testing and prototyping.
 */
public class BasketClient implements AliveEndpoint {

    /**
     * Webclient configured with the url of the basket service
     */
    private final WebClient basketClient;
    private final JSONConverter jsonConverter = new JSONConverter();

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

    public void payBasket(UUID basketId) {
        basketClient.post()
                .uri("/basket/pay-dev")
                .header("basket-id", String.valueOf(basketId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void addShoppingItems(List<ItemDto> testBasketItems, String providerSecret) {
        testBasketItems.forEach(item -> basketClient.post().uri("/items").header("provider-secret", providerSecret).body(BodyInserters.fromValue(item)).retrieve().bodyToMono(Void.class).block());
    }

    public String registerUser(String userPublicKey, String userInfo) {
        return basketClient
                .get()
                .uri("/register-user-and-obtain-serialized-registration-coupon")
                .header("user-public-key", userPublicKey)
                .header("user-info", userInfo)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public ResponseEntity<Void> sendEarn(UUID basketId, BigInteger promotionId, EarnStoreRequest earnStoreRequest) {
        var earnRequestDto = new EarnRequestStoreDto(promotionId, jsonConverter.serialize(earnStoreRequest.getRepresentation()));
        return sendBulkEarnAndSpend(new BulkRequestStoreDto(basketId, List.of(earnRequestDto), Collections.emptyList()));
    }

    public ResponseEntity<Void> sendSpend(UUID basketId, BigInteger promotionId, UUID tokenUpdateId, SpendCouponRequest spendStoreRequest, ZkpTokenUpdateMetadata metadata) {
        var spendRequestDto = new SpendRequestStoreDto(jsonConverter.serialize(spendStoreRequest.getRepresentation()),
                promotionId,
                tokenUpdateId,
                jsonConverter.serialize(new RepresentableRepresentation(metadata)));
        return sendBulkEarnAndSpend(
                new BulkRequestStoreDto(basketId,
                        Collections.emptyList(),
                        List.of(spendRequestDto)));
    }

    public ResponseEntity<Void> sendBulkEarnAndSpend(BulkRequestStoreDto bulkRequestStore) {
        return basketClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStore))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public BulkResultsStoreDto retrieveBulkResponse(UUID basketId) {
        return basketClient.get()
                .uri("/bulk-results")
                .header("basket-id", String.valueOf(basketId))
                .retrieve()
                .bodyToMono(BulkResultsStoreDto.class)
                .block();

    }

    public Mono<ResponseEntity<Void>> addPromotions(List<Promotion> promotions, String providerSecret) {
        return basketClient.post()
                .uri("/promotions")
                .header("store-secret", providerSecret)
                .body(BodyInserters.fromValue(serializedPromotionsRepresentable(promotions)))
                .retrieve()
                .toBodilessEntity();
    }

    private List<String> serializedPromotionsRepresentable(List<Promotion> promotions) {
        return promotions.stream().map(p -> jsonConverter.serialize(new RepresentableRepresentation(p))).collect(Collectors.toList());
    }
}
