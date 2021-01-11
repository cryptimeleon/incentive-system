package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.protocolmock.issue.IssueRequest;
import de.upb.crypto.incentive.protocolmock.issue.IssueResponse;
import de.upb.crypto.incentive.protocolmock.model.Token;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, IssueRequest issueRequest) {
        return uriBuilder
                .path("/issue")
                .queryParam("id", issueRequest.getId())
                .build();
    }

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new IssueRequest(42)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IssueResponse.class)
                .isEqualTo(new IssueResponse(42, new Token(0)));
    }
}