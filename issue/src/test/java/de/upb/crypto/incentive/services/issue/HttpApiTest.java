package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.procotols.issue.IssueHelper;
import de.upb.crypto.incentive.procotols.issue.IssueRequest;
import de.upb.crypto.incentive.procotols.issue.IssueResponse;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    private IssueHelper issueHelper = new IssueHelper();

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, IssueRequest issueRequest) {
        return uriBuilder
                .path("/issue")
                .queryParam("id", issueRequest.getId())
                .queryParam("payload", issueRequest.getPayload())
                .build();
    }

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, issueHelper.validRequest))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IssueResponse.class)
                .isEqualTo(issueHelper.validResponse);
    }

    @Test
    void invalidRequestTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, issueHelper.invalidRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}