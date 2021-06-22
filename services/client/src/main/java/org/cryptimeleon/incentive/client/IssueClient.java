package org.cryptimeleon.incentive.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client calls for info service.
 * Can be used for testing and prototyping.
 */
public class IssueClient {

    /**
     * Webclient configured with the url of the issue service
     */
    private WebClient issueClient;

    public IssueClient(String issueServiceUrl) {
        this.issueClient = WebClientHelper.buildWebClient(issueServiceUrl);
    }

    /**
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return issueClient.get()
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
        return issueClient.get()
                .uri(uriBuilder -> uriBuilder.path("/issue").build())
                .header("public-key", serializedUserPublicKey)
                .header("join-request", serializedJoinRequest)
                .retrieve()
                .bodyToMono(String.class);
    }
}
