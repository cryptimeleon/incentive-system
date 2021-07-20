package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.GetEarnResponseDto;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;


public class CreditClient {

    /*
     * Webclient configured with the url of the credit service
     */
    private WebClient creditClient;


    public CreditClient(String creditServiceUrl) {
        this.creditClient = WebClientHelper.buildWebClient(creditServiceUrl);
    }

    /*
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return creditClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    /*
     * Sends an earn request
     */
    public Mono<GetEarnResponseDto> sendEarnRequest(UUID requestId, String serializedRequest, UUID basketId) {
        return creditClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credit")
                        .queryParam("id", requestId)
                        .queryParam("serializedEarnRequest", serializedRequest)
                        .queryParam("basketId", basketId)
                        .build())
                .retrieve()
                .bodyToMono(GetEarnResponseDto.class);
    }
}
