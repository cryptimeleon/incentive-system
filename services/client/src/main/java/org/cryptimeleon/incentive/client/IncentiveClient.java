package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
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
public class IncentiveClient {

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
                .uri(uriBuilder -> uriBuilder.path("/join-promotion").build())
                .header("promotion-id", String.valueOf(promotionId))
                .header("user-public-key", serializedUserPublicKey)
                .header("join-request", serializedJoinRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Sends an earn request
     *
     * @param serializedEarnRequest the serialized earn request
     * @param basketId              the serialized basket id
     */
    public Mono<String> sendEarnRequest(String serializedEarnRequest, UUID basketId) {
        return incentiveClient.post()
                .uri(uriBuilder -> uriBuilder.path("/earn").build())
                .header("earn-request", serializedEarnRequest)
                .header("basket-id", basketId.toString())
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<List<Promotion>> queryPromotions() {
        return incentiveClient.get()
                .uri(uriBuilder -> uriBuilder.path("/promotions").build())
                .retrieve()
                .toEntityList(String.class)
                .map(s ->
                        s.getBody().stream().map(it ->
                                (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(it)).recreateRepresentable()
                        ).collect(Collectors.toList())
                );
    }

    public Mono<ResponseEntity<Void>> addPromotions(List<Promotion> promotions) {
        return incentiveClient.post()
                .uri(uriBuilder -> uriBuilder.path("/promotions").build())
                .body(BodyInserters.fromValue(promotions.stream().map(p -> jsonConverter.serialize(p.getRepresentation())).collect(Collectors.toList())))
                .retrieve()
                .toBodilessEntity();
    }
}
