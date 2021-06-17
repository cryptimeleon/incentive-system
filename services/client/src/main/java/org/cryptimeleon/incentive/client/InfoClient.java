package org.cryptimeleon.incentive.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class InfoClient {

    /*
     * Webclient configured with the url of the info service
     */
    private WebClient infoClient;


    public InfoClient(String creditServiceUrl) {
        this.infoClient = WebClientHelper.buildWebClient(creditServiceUrl);
    }

    /**
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return infoClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> querySerializedPublicParameters() {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/public-parameters")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> querySerializedProviderPublicKey() {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/provider-public-key")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> querySerializedProviderSecretKey(String providerSharedSecret) {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/provider-secret-key")
                        .build())
                .header("shared-secret", providerSharedSecret)
                .retrieve()
                .bodyToMono(String.class);
    }
}
