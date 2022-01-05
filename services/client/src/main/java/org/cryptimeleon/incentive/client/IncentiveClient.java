package org.cryptimeleon.incentive.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Client calls for incentive service.
 * Can be used for testing and prototyping.
 */
public class IncentiveClient {

    /**
     * Webclient configured with the url of the issue service
     */
    private WebClient incentiveClient;

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
    public Mono<String> sendJoinRequest(String serializedJoinRequest, String serializedUserPublicKey) {
        return incentiveClient.get()
                .uri(uriBuilder -> uriBuilder.path("/issue").build())
                .header("public-key", serializedUserPublicKey)
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
        return incentiveClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credit").build())
                .header("earn-request", serializedEarnRequest)
                .header("basket-id", basketId.toString())
                .retrieve()
                .bodyToMono(String.class);
    }
}
