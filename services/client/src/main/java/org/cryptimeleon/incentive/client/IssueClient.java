package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.JoinRequestDto;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class IssueClient {

    /*
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
     * @param joinRequestDto the DTO of the joinRequest to send
     * @return mono of the server's answer
     */
    public Mono<String> sendJoinRequest(JoinRequestDto joinRequestDto) {
        return issueClient.get()
                .uri(uriBuilder -> uriBuilder.path("/issue")
                        .queryParam("serializedUserPublicKey", joinRequestDto.getSerializedUserPublicKey())
                        .queryParam("serializedJoinRequest", joinRequestDto.getSerializedJoinRequest())
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
