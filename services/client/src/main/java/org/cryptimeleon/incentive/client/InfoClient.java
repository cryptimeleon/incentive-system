package org.cryptimeleon.incentive.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client calls for info service.
 * Can be used for testing and prototyping.
 */
public class InfoClient implements AliveEndpoint {

    /**
     * Webclient configured with the url of the info service
     */
    private final WebClient infoClient;


    public InfoClient(String infoServiceUrl) {
        this.infoClient = WebClientHelper.buildWebClient(infoServiceUrl);
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

    /**
     * Query the public parameters.
     *
     * @return serialized public parameters
     */
    public Mono<String> querySerializedPublicParameters() {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/public-parameters")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Query provider public key.
     *
     * @return serialized provider public key
     */
    public Mono<String> querySerializedProviderPublicKey() {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/provider-public-key")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Query provider secret key.
     * Authenticated request. Only for distributing secret key between services.
     *
     * @param providerSharedSecret shared secret for authentication
     * @return serialized provider secret key
     */
    public Mono<String> querySerializedProviderSecretKey(String providerSharedSecret) {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/provider-secret-key")
                        .build())
                .header("shared-secret", providerSharedSecret)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> querySerializedStorePublicKey() {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/store-public-key")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
    public Mono<String> querySerializedStoreSecretKey(String storeSharedSecret) {
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder.path("/store-secret-key")
                        .build())
                .header("shared-secret", storeSharedSecret)
                .retrieve()
                .bodyToMono(String.class);
    }
}
