package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client calls for incentive service.
 * Can be used for testing and prototyping.
 */
public class IncentiveClient implements AliveEndpoint {

    /**
     * Webclient configured with the url of the issue service
     */
    private WebClient incentiveClient;
    private JSONConverter jsonConverter = new JSONConverter();

    public IncentiveClient(String incentiveServiceUrl) {
        this.incentiveClient = WebClientHelper.buildWebClient(incentiveServiceUrl);
    }

    /**
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return incentiveClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Creates a join request.
     *
     * @param serializedUserPublicKey the serialized public key of the user
     * @param serializedJoinRequest   the serialized join request
     * @return mono of the server's answer
     */
    public Mono<String> sendJoinRequest(String serializedJoinRequest, String serializedUserPublicKey, BigInteger promotionId) {
        return incentiveClient.post()
                .uri("/join-promotion")
                .header("promotion-id", String.valueOf(promotionId))
                .header("user-public-key", serializedUserPublicKey)
                .header("join-request", serializedJoinRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<Void> sendBulkUpdates(UUID basketId, BulkRequestDto bulkRequestDto) {
        return incentiveClient.post()
                .uri("/bulk-token-updates")
                .header("basket-id", basketId.toString())
                .body(BodyInserters.fromValue(bulkRequestDto))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<TokenUpdateResultsDto> retrieveBulkResults(UUID basketId) {
        return incentiveClient.post()
                .uri("/bulk-token-update-results")
                .header("basket-id", basketId.toString())
                .retrieve()
                .bodyToMono(TokenUpdateResultsDto.class);
    }


    public Mono<List<Promotion>> queryPromotions() {
        return incentiveClient.get()
                .uri("/promotions")
                .retrieve()
                .toEntityList(String.class)
                .map(s ->
                        s.getBody().stream().map(it ->
                                (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(it)).recreateRepresentable()
                        ).collect(Collectors.toList())
                );
    }

    public Mono<ResponseEntity<Void>> addPromotions(List<Promotion> promotions, String providerSecret) {
        return incentiveClient.post()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(serializedPromotionsRepresentable(promotions)))
                .retrieve()
                .toBodilessEntity();
    }

    private List<String> serializedPromotionsRepresentable(List<Promotion> promotions) {
        return promotions.stream().map(p -> jsonConverter.serialize(new RepresentableRepresentation(p))).collect(Collectors.toList());
    }

    public Mono<ResponseEntity<Void>> deleteAllPromotions(String providerSecret) {
        return incentiveClient.delete()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .retrieve()
                .toBodilessEntity();
    }

    public Mono<ResponseEntity<String>> genesis(UserPreKeyPair userPreKeyPair) {
        return incentiveClient.post()
                .uri("/genesis")
                .header("user-public-key", jsonConverter.serialize(userPreKeyPair.getPk().getRepresentation()))
                .retrieve()
                .bodyToMono((new ParameterizedTypeReference<ResponseEntity<String>>() {}));
    }
}
