package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.protocoldefinition.issuejoin.IssueResponse;
import de.upb.crypto.incentive.protocoldefinition.issuejoin.JoinRequest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import java.util.UUID;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, JoinRequest joinRequest) {
        return uriBuilder
                .path("/issue")
                .queryParam("id", joinRequest.getId())
                .queryParam("serializedJoinRequest", joinRequest.getSerializedJoinRequest())
                .build();
    }

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        UUID id = UUID.randomUUID();
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new JoinRequest(id, "Some join request")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IssueResponse.class)
                .isEqualTo(new IssueResponse(id, "Some serialized response"));
    }
}