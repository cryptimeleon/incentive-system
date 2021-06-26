package org.cryptimeleon.incentive.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * Client calls for credit service.
 * Can be used for testing and prototyping.
 */
public class CreditClient {

    /**
     * Webclient configured with the url of the credit service
     */
    private WebClient creditClient;


    public CreditClient(String creditServiceUrl) {
        this.creditClient = WebClientHelper.buildWebClient(creditServiceUrl);
    }

    /**
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return creditClient.get()
                .uri("/")
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
        return creditClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credit").build())
                .header("earn-request", serializedEarnRequest)
                .header("basket-id", basketId.toString())
                .retrieve()
                .bodyToMono(String.class);
    }
}
